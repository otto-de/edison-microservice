package de.otto.edison.health.configuration;

import de.otto.edison.health.indicator.ApplicationHealthIndicator;
import de.otto.edison.health.indicator.GracefulShutdownHealthIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthConfiguration {

    @Value("${edison.graceful.shutdown.time.beforeIndicateError:5000}")
    private long timeBeforeIndicateError;

    @Value("${edison.graceful.shutdown.time.phaseOut:25000}")
    private long timeForPhaseOut;

    @Bean
    @ConditionalOnProperty(name = "edison.graceful.shutdown.active", havingValue = "true")
    public GracefulShutdownHealthIndicator gracefulShutdownHealthIndicator() {
        return new GracefulShutdownHealthIndicator(timeBeforeIndicateError, timeForPhaseOut);
    }

    @Bean
    public ApplicationHealthIndicator healthIndicator() {
        return new ApplicationHealthIndicator();
    }

}
