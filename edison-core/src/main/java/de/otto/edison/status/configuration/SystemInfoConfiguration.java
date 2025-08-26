package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.SystemInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@ConditionalOnProperty(name = "edison.status.aws.enabled", havingValue = "false", matchIfMissing = true)
public class SystemInfoConfiguration {

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
    @ConditionalOnMissingBean(SystemInfo.class)
    public SystemInfo systemInfo() {
        return SystemInfo.systemInfo(hostname(), port);
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
