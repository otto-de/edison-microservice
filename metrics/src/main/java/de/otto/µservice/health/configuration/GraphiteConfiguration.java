package de.otto.µservice.health.configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.graphite.GraphiteReporter.forRegistry;
import static java.lang.Integer.valueOf;
import static java.lang.String.join;
import static java.net.InetAddress.getLocalHost;
import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Guido Steinacker
 * @since 19.02.15
 */
@Configuration
@ConditionalOnProperty(name = {
        "metrics.graphite.host",
        "metrics.graphite.port",
        "metrics.graphite.prefix"}
)
public class GraphiteConfiguration {

    private static final Logger LOG = getLogger(GraphiteConfiguration.class);

    @Value("${metrics.graphite.host}")
    private String graphiteHost;
    @Value("${metrics.graphite.port}")
    private String graphitePort;
    @Value("${metrics.graphite.prefix}")
    private String graphitePrefix;

    @Autowired
    private MetricRegistry metricRegistry;

    @Bean
    public GraphiteReporter graphiteReporter() {
        final InetSocketAddress address = new InetSocketAddress(graphiteHost, valueOf(graphitePort));
        final GraphiteReporter graphiteReporter = forRegistry(metricRegistry)
                .prefixedWith(graphitePrefix + "." + reverse(hostName()) + ".metrics")
                .build(new Graphite(address));
        graphiteReporter.start(1, TimeUnit.MINUTES);
        return graphiteReporter;
    }

    private static String reverse(final String host) {
        final List<String> parts = asList(host.split("\\."));
        Collections.reverse(parts);
        return join(".", parts);
    }

    private static String hostName() {
        try {
            return getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            String msg = "Error resolving canonical name of localhost";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

}
