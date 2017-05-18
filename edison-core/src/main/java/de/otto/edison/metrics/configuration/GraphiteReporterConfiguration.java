package de.otto.edison.metrics.configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.GraphiteReporter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
 * Configuration to report metrics to Graphite.
 *
 * @author Guido Steinacker
 * @since 19.02.15
 */
@Configuration
@EnableConfigurationProperties(MetricsProperties.class)
@ConditionalOnProperty(
        prefix = "edison.metrics.graphite",
        name = {"host", "port", "prefix"})
public class GraphiteReporterConfiguration {

    private static final Logger LOG = getLogger(GraphiteReporterConfiguration.class);

    private final MetricRegistry metricRegistry;
    private final MetricsProperties.Graphite graphiteMetricsProperties;

    @Autowired
    public GraphiteReporterConfiguration(final MetricRegistry metricRegistry,
                                         final MetricsProperties graphiteMetricsProperties) {
        this.metricRegistry = metricRegistry;
        this.graphiteMetricsProperties = graphiteMetricsProperties.getGraphite();
    }

    @Bean
    public GraphiteReporter graphiteReporter() {
        final InetSocketAddress address = new InetSocketAddress(graphiteMetricsProperties.getHost(), valueOf(graphiteMetricsProperties.getPort()));
        final String prefix = graphiteMetricsProperties.isAddHostToPrefix() ?
                graphiteMetricsProperties.getPrefix() + "." + reverse(hostName()) + ".metrics" : graphiteMetricsProperties.getPrefix();
        final GraphiteReporter graphiteReporter = forRegistry(metricRegistry)
                .prefixedWith(prefix)
                .build(new com.codahale.metrics.graphite.Graphite(address));
        graphiteReporter.start(1, TimeUnit.MINUTES);
        return graphiteReporter;
    }

    private static String reverse(final String host) {
        final List<String> parts = asList(host.split("\\."));
        Collections.reverse(parts);
        return join(".", parts);
    }

    private static String hostName() {

        final String envHost = System.getenv("HOST");
        if (envHost != null) {
            return envHost;
        }
        try {
            return getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            String msg = "Error resolving canonical name of localhost";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

}
