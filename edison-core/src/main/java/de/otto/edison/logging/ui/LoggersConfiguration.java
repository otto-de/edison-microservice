package de.otto.edison.logging.ui;

import de.otto.edison.configuration.EdisonApplicationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @since 1.1.0
 */
@Configuration
@ConditionalOnProperty(prefix = "edison.logging.ui", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(EdisonApplicationProperties.class)
public class LoggersConfiguration {

    @Bean
    public static DisableEndpointPostProcessor loggersPropertySource() {
        return new DisableEndpointPostProcessor("loggers");
    }

}
