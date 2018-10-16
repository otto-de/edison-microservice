package de.otto.edison.authentication.configuration;

import de.otto.edison.authentication.LdapAuthenticationFilter;
import de.otto.edison.authentication.LdapRoleAuthenticationFilter;
import de.otto.edison.authentication.connection.LdapConnectionFactory;
import de.otto.edison.authentication.connection.SSLLdapConnectionFactory;
import de.otto.edison.authentication.connection.StartTlsLdapConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration for LDAP authentication. Secures specific endpoints according to the {@code edison.ldap} configuration
 * as given in {@link LdapProperties}}.
 */
@Configuration
@EnableConfigurationProperties(LdapProperties.class)
@ConditionalOnProperty(prefix = "edison.ldap", name = "enabled", havingValue = "true")
@ConditionalOnMissingBean(name = {"ldapAuthenticationFilter"})
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
    @ConditionalOnMissingBean(LdapConnectionFactory.class)
    public LdapConnectionFactory ldapConnectionFactory(final LdapProperties ldapProperties) {
        if (ldapProperties.getEncryptionType() == EncryptionType.SSL) {
            return new SSLLdapConnectionFactory(ldapProperties);
        }
        return new StartTlsLdapConnectionFactory(ldapProperties);
    }

    /**
     * Add an authentication filter to the web application context if edison.ldap property is set to {@code enabled}'.
     * All routes starting with the value of the {@code edison.ldap.prefix} property will be secured by LDAP. If no
     * property is set this will default to all routes starting with '/internal'.
     *
     * @param ldapProperties the properties used to configure LDAP
     * @param ldapConnectionFactory the connection factory used to build the LdapAuthenticationFilter
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<LdapAuthenticationFilter> ldapAuthenticationFilter(final LdapProperties ldapProperties,
                                                           final LdapConnectionFactory ldapConnectionFactory) {
        FilterRegistrationBean<LdapAuthenticationFilter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setFilter(new LdapAuthenticationFilter(ldapProperties, ldapConnectionFactory));
        filterRegistration.setOrder(Ordered.LOWEST_PRECEDENCE - 1);
        ldapProperties.getPrefixes().forEach(prefix -> filterRegistration.addUrlPatterns(String.format("%s/*", prefix)));
        return filterRegistration;
    }

    @Bean
    @ConditionalOnProperty(prefix = "edison.ldap", name = "required-role")
    public FilterRegistrationBean<LdapRoleAuthenticationFilter> ldapRoleAuthenticationFilter(final LdapProperties ldapProperties) {
        FilterRegistrationBean<LdapRoleAuthenticationFilter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setFilter(new LdapRoleAuthenticationFilter(ldapProperties));
        filterRegistration.setOrder(Ordered.LOWEST_PRECEDENCE);
        ldapProperties.getPrefixes().forEach(prefix -> filterRegistration.addUrlPatterns(String.format("%s/*", prefix)));
        return filterRegistration;
    }
}
