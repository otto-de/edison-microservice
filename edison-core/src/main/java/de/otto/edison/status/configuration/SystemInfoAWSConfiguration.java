package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;
import software.amazon.awssdk.imds.Ec2MetadataAsyncClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "edison.status.aws.enabled", havingValue = "true")
public class SystemInfoAWSConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInfoAWSConfiguration.class);

    private static final String defaultHostname;

    static {
        String localHost = null;
        try {
            localHost = InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException ignored) {
        }
        defaultHostname = localHost;
    }

    @Value("${HOSTNAME:}")
    private String hostname;
    @Value("${server.hostname:}")
    private String envhostname;
    @Value("${server.port:8080}")
    private int port;

    @Bean
    public Ec2MetadataAsyncClient ec2MetadataAsyncClient() {
        return Ec2MetadataAsyncClient.create();
    }

    @Bean
    public SystemInfo systemInfo(@Autowired Ec2MetadataAsyncClient ec2MetadataAsyncClient) {

        ArrayList<String> additionalInfo = new ArrayList<>();
        try {
            additionalInfo.add("Instance-ID: " + ec2MetadataAsyncClient.get("/latest/meta-data/instance-id").get().asString());
            additionalInfo.add("Instance-Type: " + ec2MetadataAsyncClient.get("/latest/meta-data/instance-type").get().asString());
        } catch (Exception e) {
            LOG.warn("Could not get Instance-ID from Ec2MetadataAsyncClient, likely because I'm not running on an EC2 instance.", e);
        }

        return SystemInfo.systemInfo(hostname(), port, List.copyOf(additionalInfo));

    }

    private String hostname() {
        if (!ObjectUtils.isEmpty(envhostname)) {
            return envhostname;
        }
        if (!ObjectUtils.isEmpty(hostname)) {
            return hostname;
        }
        return defaultHostname;
    }
}
