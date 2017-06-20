package de.otto.edison.authentication.configuration;

import de.otto.edison.authentication.LdapAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for LDAP authentication. Secures specific endpoints according to the {@code edison.ldap} configuration
 * as given in {@link LdapProperties}}.
 */
@Configuration
@EnableConfigurationProperties(LdapProperties.class)
@ConditionalOnProperty(prefix = "edison.ldap", name = "enabled", havingValue = "true")
@ConditionalOnMissingBean(name = {"ldapAuthenticationFilter", "togglzAuthenticationFilter"})
//TODO: remove deprecated togglzAuthenticationFilter from ConditionalOnMissingBean in Edison 2.0.0
public class LdapConfiguration {

    /**
     * Add an authentication filter to the web application context if edison.ldap property is set to {@code enabled}'.
     * All routes starting with the value of the {@code edison.ldap.prefix} property will be secured by LDAP. If no
     * property is set this will default to all routes starting with '/internal'.
     *
     * @param ldapProperties the properties used to configure LDAP
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean ldapAuthenticationFilter(final LdapProperties ldapProperties) {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new LdapAuthenticationFilter(ldapProperties));
        filterRegistration.addUrlPatterns(String.format("%s/*", ldapProperties.getPrefix()));
        return filterRegistration;
    }
}
