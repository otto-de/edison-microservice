package de.otto.edison.metrics.configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.codahale.metrics.Slf4jReporter.LoggingLevel.INFO;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableMetrics
@EnableConfigurationProperties(MetricsProperties.class)
@ConditionalOnProperty(name = "edison.metrics.slf4j.logger")
public class Slf4JReporterConfiguration extends MetricsConfigurerAdapter {

    private final MetricsProperties.Slf4j slf4jProperties;

    @Autowired
    public Slf4JReporterConfiguration(final MetricsProperties metricsProperties) {
        this.slf4jProperties = metricsProperties.getSlf4j();
    }

    @Override
    public void configureReporters(final MetricRegistry metricRegistry) {
        Slf4jReporter
                .forRegistry(metricRegistry)
                .outputTo(getLogger(slf4jProperties.getLogger()))
                .withLoggingLevel(INFO)
                .build()
                .start(slf4jProperties.getPeriod(), MINUTES);
    }

}
