package de.otto.edison.health.configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import static com.codahale.metrics.Slf4jReporter.LoggingLevel.INFO;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableMetrics
//@ConditionalOnProperty(name = "edison.metrics.slf4j.logger")
public class MetricsConfiguration extends MetricsConfigurerAdapter {

    @Value("${edison.metrics.slf4j.logger}")
    private String logger;

    @Override
    public void configureReporters(final MetricRegistry metricRegistry) {
        Slf4jReporter
                .forRegistry(metricRegistry)
                .outputTo(getLogger(logger))
                .withLoggingLevel(INFO)
                .build()
                .start(1, MINUTES);
    }

}
