package de.otto.edison.status.configuration;

import de.otto.edison.annotations.Beta;
import de.otto.edison.status.domain.StatusPropertiesInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StatusPropertiesInfoProperties.class)
@Beta
public class StastusPropertiesInfoConfiguration {

    private final StatusPropertiesInfoProperties statusPropertiesInfoProperties;

    public StastusPropertiesInfoConfiguration(final StatusPropertiesInfoProperties statusPropertiesInfoProperties) {
        this.statusPropertiesInfoProperties = statusPropertiesInfoProperties;
    }

    @Bean
    @ConditionalOnMissingBean(StatusPropertiesInfo.class)
    public StatusPropertiesInfo extendedPropertiesInfo() {
        return StatusPropertiesInfo.extendedInfo(statusPropertiesInfoProperties);
    }

}
