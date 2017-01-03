package de.otto.edison.example.configuration;

import de.otto.edison.navigation.NavBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static de.otto.edison.navigation.NavBarItem.navBarItem;
import static de.otto.edison.navigation.NavBarItem.top;

@Component
public class NavigationConfiguration {

    @Autowired
    public NavigationConfiguration(final NavBar mainNavBar) {
        mainNavBar.register(navBarItem(top(), "Status", "/internal/status"));
    }
}
