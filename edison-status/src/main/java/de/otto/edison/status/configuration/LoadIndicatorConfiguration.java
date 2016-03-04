package de.otto.edison.status.configuration;

import com.codahale.metrics.MetricRegistry;
import de.otto.edison.annotations.Beta;
import de.otto.edison.status.indicator.load.EverythingFineStrategy;
import de.otto.edison.status.indicator.load.LoadDetector;
import de.otto.edison.status.indicator.load.MetricCounterStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Beta
@Configuration
public class LoadIndicatorConfiguration {

    @Autowired(required = false)
    private MetricRegistry metricRegistry;

    @Value("${edison.status.load.metrics.counterName:}")
    private String counterName;

    @Value("${edison.status.load.metrics.minThreshold:0}")
    private long minThreshold;

    @Value("${edison.status.load.metrics.maxThreshold:-1}")
    private long maxThreshold;

    @Bean
    @ConditionalOnProperty(name = "edison.status.load.strategy", havingValue = "MetricCounter")
    public LoadDetector metricCounterStrategy() {
        return new MetricCounterStrategy(metricRegistry, counterName, minThreshold, maxThreshold);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadDetector defaultStrategy() {
        return new EverythingFineStrategy();
    }

}
