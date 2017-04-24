package de.otto.edison.loggers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @since 1.1.0
 */
@Configuration
@ConditionalOnProperty(prefix = "edison.loggers", name = "enabled", matchIfMissing = true)
public class LoggersConfiguration {

    @Bean
    public DisableEndpointPostProcessor loggersPropertySource() {
        return new DisableEndpointPostProcessor("loggers");
    }

}
