package de.otto.edison.logging.ui;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @since 1.1.0
 */
@Configuration
@ConditionalOnProperty(prefix = "edison.logging.ui", name = "enabled", matchIfMissing = true)
public class LoggersConfiguration {

    @Bean
    public static DisableEndpointPostProcessor loggersPropertySource() {
        return new DisableEndpointPostProcessor("loggers");
    }

}
