package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.ApplicationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of the information about this application.
 *
 * This is used for /internal/status and /internal/about.
 */
@Configuration
public class ApplicationInfoConfiguration {

    @Value("${spring.application.name:unknown}")
    private String name;
    @Value("${edison.status.application.description:}")
    private String description;
    @Value("${edison.status.application.group:}")
    private String group;
    @Value("${edison.status.application.environment:}")
    private String environment;

    @Bean
    @ConditionalOnMissingBean(ApplicationInfo.class)
    public ApplicationInfo applicationInfo() {
        return ApplicationInfo.applicationInfo(name, description, group, environment);
    }

}
