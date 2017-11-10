package de.otto.edison.example.configuration;

import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.navigation.NavBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import static de.otto.edison.navigation.NavBarItem.navBarItem;
import static java.lang.String.format;

@Component
@EnableConfigurationProperties(EdisonApplicationProperties.class)
public class NavigationConfiguration {

    @Autowired
    public NavigationConfiguration(final NavBar mainNavBar,
                                   final EdisonApplicationProperties properties) {
        mainNavBar.register(navBarItem(0, "Home", "/"));
        mainNavBar.register(navBarItem(1, "Feature Toggles", format("%s/toggles/console/index", properties.getManagement().getBasePath())));
    }
}
