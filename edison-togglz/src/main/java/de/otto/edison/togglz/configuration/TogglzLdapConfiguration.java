package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.authentication.LdapAuthenticationFilter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @deprecated Use {@link de.otto.edison.authentication.LdapAuthenticationFilter} instead of this.
 * This class will be removed in 2.0.0
 */
@Configuration
@EnableConfigurationProperties(TogglzLdapProperties.class)
@ConditionalOnProperty(prefix = "edison.togglz.console.ldap", name = "enabled", havingValue = "true")
@ConditionalOnMissingBean(name = "togglzAuthenticationFilter")
@Deprecated
public class TogglzLdapConfiguration {

    public static final String TOGGLES_URL_PATTERN = "/toggles/console/*";
    private static final Logger LOG = getLogger(TogglzLdapConfiguration.class);

    @Bean
    public FilterRegistrationBean togglzAuthenticationFilter(final @Value("${management.context-path:/internal}") String prefix,
                                                             final TogglzLdapProperties ldapProperties) {
        LOG.warn("================================================================");
        LOG.warn("TogglzLdapConfiguration.togglzAuthenticationFilter is deprecated and should be replaced by de.otto.edison.authentication.LdapAuthenticationFilter");
        LOG.warn("================================================================");
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new LdapAuthenticationFilter(ldapProperties));
        filterRegistration.addUrlPatterns(prefix + TOGGLES_URL_PATTERN);
        return filterRegistration;
    }
}
