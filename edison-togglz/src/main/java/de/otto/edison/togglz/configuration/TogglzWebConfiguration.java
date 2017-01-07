package de.otto.edison.togglz.configuration;

import de.otto.edison.navigation.NavBar;
import de.otto.edison.togglz.authentication.LdapAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.console.TogglzConsoleServlet;
import org.togglz.servlet.TogglzFilter;

import static de.otto.edison.navigation.NavBarItem.bottom;
import static de.otto.edison.navigation.NavBarItem.navBarItem;

@Configuration
@EnableConfigurationProperties(LdapProperties.class)
public class TogglzWebConfiguration {

    public static final String TOGGLES_URL_PATTERN = "/toggles/console/*";

    @Bean
    @ConditionalOnMissingBean(name = "togglzFilter")
    public FilterRegistrationBean togglzFilter() {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new TogglzFilter());
        filterRegistration.addUrlPatterns("/*");
        return filterRegistration;
    }

    @Bean
    @ConditionalOnProperty(prefix = "edison.togglz.ldap-authentication", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(name = "togglzAuthenticationFilter")
    public FilterRegistrationBean togglzAuthenticationFilter(final @Value("${management.context-path:/internal}") String prefix,
                                                             final LdapProperties ldapProperties) {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new LdapAuthenticationFilter(ldapProperties));
        filterRegistration.addUrlPatterns(prefix + TOGGLES_URL_PATTERN);
        return filterRegistration;
    }

    @Bean
    @ConditionalOnProperty(name = "edison.togglz.web.console", havingValue = "true", matchIfMissing = true)
    public ServletRegistrationBean togglzServlet(final @Value("${management.context-path:/internal}") String prefix,
                                                 final NavBar rightNavBar) {
        rightNavBar.register(navBarItem(bottom(), "Feature Toggles", prefix + "/toggles/console"));
        return new ServletRegistrationBean(new TogglzConsoleServlet(), prefix + TOGGLES_URL_PATTERN);
    }
}
