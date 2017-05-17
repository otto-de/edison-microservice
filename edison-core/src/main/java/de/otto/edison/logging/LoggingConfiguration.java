package de.otto.edison.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@ConditionalOnProperty(prefix = "edison.logging", name = "header.enabled")
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingConfiguration {

    @Bean
    public LogHeadersToMDCFilter logHeadersToMDCFilter(final LoggingProperties properties) {
        return new LogHeadersToMDCFilter(Arrays.asList(properties.getHeader().getNames().split(",")));
    }
}
