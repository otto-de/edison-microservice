package de.otto.edison.authentication.configuration;

import de.otto.edison.authentication.LdapAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LdapProperties.class)
@ConditionalOnProperty(prefix = "edison.ldap", name = "enabled", havingValue = "true")
@ConditionalOnMissingBean(name = "authenticationFilter")
public class LdapConfiguration {

    @Bean
    public FilterRegistrationBean authenticationFilter(final @Value("${edison.ldap.prefix:/internal}") String prefix,
                                                       final LdapProperties ldapProperties) {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new LdapAuthenticationFilter(ldapProperties));
        filterRegistration.addUrlPatterns(String.format("%s/*", prefix));
        return filterRegistration;
    }
}
