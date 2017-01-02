package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.VersionInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the application's version.
 * <p>
 *      VersionInfo is used to give information about the current service's version on a status page.
 * </p>
 */
@Configuration
@EnableConfigurationProperties(VersionInfoProperties.class)
public class VersionInfoConfiguration {

    @Bean
    @ConditionalOnMissingBean(VersionInfo.class)
    public VersionInfo versionInfo(final VersionInfoProperties versionInfoProperties) {
        return VersionInfo.versionInfo(versionInfoProperties);
    }

}
