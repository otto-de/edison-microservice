package de.otto.edison.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@AutoConfiguration
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "edison.logging", name = "header.enabled")
    public LogHeadersToMDCFilter logHeadersToMDCFilter(final LoggingProperties properties) {
        return new LogHeadersToMDCFilter(Arrays.asList(properties.getHeader().getNames().split(",")));
    }
}
