package de.otto.edison.example.configuration;

import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.navigation.NavBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import static de.otto.edison.navigation.NavBarItem.navBarItem;
import static de.otto.edison.navigation.NavBarItem.top;

@Component
@EnableConfigurationProperties(EdisonApplicationProperties.class)
public class NavigationConfiguration {

    @Autowired
    public NavigationConfiguration(final NavBar mainNavBar,
                                   final EdisonApplicationProperties properties) {
        mainNavBar.register(navBarItem(top(), "Status", String.format("%s/status", properties.getManagement().getBasePath())));
    }
}
