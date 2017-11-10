package de.otto.edison.status.configuration;

import de.otto.edison.configuration.EdisonApplicationProperties;
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
@EnableConfigurationProperties(EdisonApplicationProperties.class)
public class ApplicationInfoConfiguration {

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @Bean
    @ConditionalOnMissingBean(ApplicationInfo.class)
    public ApplicationInfo applicationInfo(EdisonApplicationProperties edisonApplicationProperties) {
        return ApplicationInfo.applicationInfo(serviceName, edisonApplicationProperties);
    }

}
