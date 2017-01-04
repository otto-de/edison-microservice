package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.ApplicationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the information about this application.
 *
 * This is used for /internal/status
 */
@Configuration
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationInfoConfiguration {

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @Bean
    @ConditionalOnMissingBean(ApplicationInfo.class)
    public ApplicationInfo applicationInfo(ApplicationProperties applicationProperties) {
        return ApplicationInfo.applicationInfo(serviceName, applicationProperties);
    }

}
