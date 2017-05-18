package de.otto.edison.metrics.configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import de.otto.edison.metrics.sender.FilteringGraphiteSender;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

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

    @Bean
    public GraphiteReporter graphiteReporter(final MetricRegistry metricRegistry,
                                             final MetricsProperties metricsProperties,
                                             final Predicate<String> graphiteFilterPredicate) {
        final MetricsProperties.Graphite graphiteMetricsProperties = metricsProperties.getGraphite();
        final String prefix = graphiteMetricsProperties.isAddHostToPrefix()
                ? graphiteMetricsProperties.getPrefix() + "." + reverse(hostName()) + ".metrics"
                : graphiteMetricsProperties.getPrefix();
        final GraphiteReporter graphiteReporter = forRegistry(metricRegistry)
                .prefixedWith(prefix)
                .build(graphiteSender(graphiteMetricsProperties, graphiteFilterPredicate));
        graphiteReporter.start(1, TimeUnit.MINUTES);
        return graphiteReporter;
    }

    private GraphiteSender graphiteSender(final MetricsProperties.Graphite graphiteMetricsProperties,
                                          final Predicate<String> graphiteFilterPredicate) {
        final InetSocketAddress address = new InetSocketAddress(graphiteMetricsProperties.getHost(), valueOf(graphiteMetricsProperties.getPort()));
        return new FilteringGraphiteSender(new Graphite(address), graphiteFilterPredicate);
    }

    /**
     * This implementation provides a filter Predicate&lt;String&gt; if no Bean 'graphiteFilterPredicate' is
     * defined. It removes all metric values that have a postfix of .m5_rate, .m_15_rate, ...
     * If you want to override this behaviour you can define a bean 'graphiteFilterPredicate' with an own
     * implementation. All Predicate executions that return true are sent.
     *
     * @return graphiteFilterPredicate
     */
    @Bean
    @ConditionalOnMissingBean
    public Predicate<String> graphiteFilterPredicate() {
        return FilteringGraphiteSender.removePostfixValues(".m5_rate", ".m15_rate", ".min", ".max", ".mean_rate", ".p50", ".p75", ".p98", ".stddev");
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
