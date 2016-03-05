package de.otto.edison.metrics.configuration;

import com.codahale.metrics.MetricRegistry;
import de.otto.edison.annotations.Beta;
import de.otto.edison.metrics.load.EverythingFineStrategy;
import de.otto.edison.metrics.load.LoadDetector;
import de.otto.edison.metrics.load.MetricTimerStrategy;
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

    @Value("${edison.metrics.load.timerName:}")
    private String timerName;

    @Value("${edison.metrics.load.minThreshold:0}")
    private long minThreshold;

    @Value("${edison.metrics.load.maxThreshold:-1}")
    private long maxThreshold;

    @Bean
    @ConditionalOnProperty(name = "edison.metrics.load.strategy", havingValue = "MetricCounter")
    public LoadDetector metricCounterStrategy() {
        return new MetricTimerStrategy(metricRegistry, timerName, minThreshold, maxThreshold);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadDetector defaultStrategy() {
        return new EverythingFineStrategy();
    }

}
