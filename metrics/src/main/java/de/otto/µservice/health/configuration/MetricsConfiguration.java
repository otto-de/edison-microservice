package de.otto.µservice.health.configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

import static com.codahale.metrics.Slf4jReporter.LoggingLevel.INFO;
import static com.codahale.metrics.graphite.GraphiteReporter.forRegistry;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableMetrics
public class MetricsConfiguration extends MetricsConfigurerAdapter {

    @Override
    public void configureReporters(final MetricRegistry metricRegistry) {
        Slf4jReporter
                .forRegistry(metricRegistry)
                .outputTo(getLogger("de.otto.µservice.metrics"))
                .withLoggingLevel(INFO)
                .build()
                .start(1, MINUTES);
    }

}
