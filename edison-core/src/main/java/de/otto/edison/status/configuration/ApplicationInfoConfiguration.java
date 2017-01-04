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
@EnableConfigurationProperties(StatusProperties.class)
public class ApplicationInfoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ApplicationInfo.class)
    public ApplicationInfo applicationInfo(StatusProperties statusProperties) {
        return ApplicationInfo.applicationInfo(statusProperties);
    }

}
