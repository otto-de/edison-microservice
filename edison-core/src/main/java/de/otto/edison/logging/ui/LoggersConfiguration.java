package de.otto.edison.logging.ui;

import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.navigation.NavBar;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @since 1.1.0
 */
@Configuration
@ConditionalOnProperty(name = "endpoints.loggers.enabled", havingValue = "true")
@EnableConfigurationProperties(EdisonApplicationProperties.class)
public class LoggersConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "edison.logging.ui", name = "enabled", matchIfMissing = true)
    public static DisableEndpointPostProcessor loggersPropertySource() {
        return new DisableEndpointPostProcessor("loggers");
    }

    @Bean
    @ConditionalOnProperty(prefix = "edison.logging.ui", name = "enabled", matchIfMissing = true)
    public LoggersHtmlEndpoint loggersHtmlEndpoint(final LoggersEndpoint loggersEndpoint,
                                                   final NavBar rightNavBar,
                                                   final EdisonApplicationProperties properties) {
        return new LoggersHtmlEndpoint(loggersEndpoint, rightNavBar, properties);
    }
}
