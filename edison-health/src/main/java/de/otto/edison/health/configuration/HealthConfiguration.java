package de.otto.edison.health.configuration;

import de.otto.edison.health.indicator.ApplicationHealthIndicator;
import de.otto.edison.health.indicator.GracefulShutdownHealthIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthConfiguration {

    @Value("${edison.gracefulshutdown.indicateErrorAfter:5000}")
    private long timeBeforeIndicateError;

    @Value("${edison.gracefulshutdown.phaseOutAfter:25000}")
    private long timeForPhaseOut;

    @Bean
    @ConditionalOnProperty(name = "edison.gracefulshutdown.enabled", havingValue = "true", matchIfMissing = true)
    public GracefulShutdownHealthIndicator gracefulShutdownHealthIndicator() {
        return new GracefulShutdownHealthIndicator(timeBeforeIndicateError, timeForPhaseOut);
    }

    @Bean
    public ApplicationHealthIndicator applicationHealthIndicator() {
        return new ApplicationHealthIndicator();
    }

}
