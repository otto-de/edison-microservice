package de.otto.edison.health.configuration;

import de.otto.edison.health.indicator.ApplicationHealthIndicator;
import de.otto.edison.health.indicator.GracefulShutdownHealthIndicator;
import de.otto.edison.health.indicator.GracefulShutdownProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GracefulShutdownProperties.class)
public class HealthConfiguration {

    @Bean
    @ConditionalOnProperty(name = "edison.gracefulshutdown.enabled", havingValue = "true", matchIfMissing = true)
    public GracefulShutdownHealthIndicator gracefulShutdownHealthIndicator(final GracefulShutdownProperties properties) {
        return new GracefulShutdownHealthIndicator(properties);
    }

    @Bean
    public ApplicationHealthIndicator applicationHealthIndicator() {
        return new ApplicationHealthIndicator();
    }

}
