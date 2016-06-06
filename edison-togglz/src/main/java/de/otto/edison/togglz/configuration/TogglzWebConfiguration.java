package de.otto.edison.togglz.configuration;


import de.otto.edison.togglz.authentication.LdapAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.console.TogglzConsoleServlet;
import org.togglz.servlet.TogglzFilter;

@Configuration
public class TogglzWebConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "togglzFilter")
    public FilterRegistrationBean togglzFilter() {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new TogglzFilter());
        filterRegistration.addUrlPatterns("/*");
        return filterRegistration;
    }

    @Bean
    @ConditionalOnProperty(prefix = "edison.togglz", name = "ldap-authentication.enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "togglzAuthenticationFilter")
    public FilterRegistrationBean togglzAuthenticationFilter(@Value("${management.context-path:/internal}") String prefix,
                                                             @Value("${edison.togglz.ldap-authentication.host:}") String host,
                                                             @Value("${edison.togglz.ldap-authentication.port:389}") int port,
                                                             @Value("${edison.togglz.ldap-authentication.base-dn:}") String baseDn,
                                                             @Value("${edison.togglz.ldap-authentication.rdn-identifier:}") String rdnIdentifier) {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new LdapAuthenticationFilter(host, port, baseDn, rdnIdentifier));
        filterRegistration.addUrlPatterns(prefix + "/togglz/*");
        return filterRegistration;
    }

    @Bean
    public ServletRegistrationBean togglzServlet(@Value("${management.context-path:/internal}") String prefix) {
        return new ServletRegistrationBean(new TogglzConsoleServlet(), prefix + "/togglz/*");
    }


}
