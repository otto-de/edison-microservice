package de.otto.µservice.health.configuration;

import de.otto.µservice.health.indicator.ApplicationHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthConfiguration {

    @Bean
    public ApplicationHealthIndicator healthIndicator() {
        return new ApplicationHealthIndicator();
    }

}
