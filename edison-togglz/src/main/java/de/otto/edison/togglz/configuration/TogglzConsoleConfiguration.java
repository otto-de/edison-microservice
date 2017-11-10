package de.otto.edison.togglz.configuration;

import de.otto.edison.navigation.NavBar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.console.TogglzConsoleServlet;

import static de.otto.edison.navigation.NavBarItem.bottom;
import static de.otto.edison.navigation.NavBarItem.navBarItem;

@Configuration
@EnableConfigurationProperties(value = {TogglzProperties.class})
@ConditionalOnProperty(prefix = "edison.togglz.console", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TogglzConsoleConfiguration {

    public static final String TOGGLES_URL_PATTERN = "/toggles/console/*";

    @Bean
    public ServletRegistrationBean togglzServlet(final @Value("${edison.application.management.base-path:/internal}") String prefix,
                                                 final NavBar rightNavBar) {

        // Register Togglz Console in the right "Admin" navigation bar:
        rightNavBar.register(navBarItem(bottom(), "Feature Toggles", prefix + "/toggles/console"));
        // Register TogglzConsoleServlet:
        return new ServletRegistrationBean(new TogglzConsoleServlet(), prefix + TOGGLES_URL_PATTERN);
    }
}
