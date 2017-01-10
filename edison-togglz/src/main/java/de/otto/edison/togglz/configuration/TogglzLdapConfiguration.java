package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.authentication.LdapAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TogglzLdapProperties.class)
@ConditionalOnProperty(prefix = "edison.togglz.console.ldap", name = "enabled", havingValue = "true")
@ConditionalOnMissingBean(name = "togglzAuthenticationFilter")
public class TogglzLdapConfiguration {

    public static final String TOGGLES_URL_PATTERN = "/toggles/console/*";

    @Bean
    public FilterRegistrationBean togglzAuthenticationFilter(final @Value("${management.context-path:/internal}") String prefix,
                                                             final TogglzLdapProperties ldapProperties) {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new LdapAuthenticationFilter(ldapProperties));
        filterRegistration.addUrlPatterns(prefix + TOGGLES_URL_PATTERN);
        return filterRegistration;
    }
}
