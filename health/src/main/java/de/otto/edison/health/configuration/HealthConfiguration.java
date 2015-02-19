package de.otto.edison.health.configuration;

import de.otto.edison.health.indicator.ApplicationHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthConfiguration {

    @Bean
    public ApplicationHealthIndicator healthIndicator() {
        return new ApplicationHealthIndicator();
    }

}
