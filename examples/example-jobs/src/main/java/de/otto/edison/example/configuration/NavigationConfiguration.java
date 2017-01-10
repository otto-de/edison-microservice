package de.otto.edison.example.configuration;

import de.otto.edison.navigation.NavBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static de.otto.edison.navigation.NavBarItem.navBarItem;

@Component
public class NavigationConfiguration {

    @Autowired
    public NavigationConfiguration(final NavBar mainNavBar) {
        mainNavBar.register(navBarItem(0, "Home", "/"));
        mainNavBar.register(navBarItem(1, "Status", "/internal/status"));
        mainNavBar.register(navBarItem(2, "Job Overview", "/internal/jobs"));
        mainNavBar.register(navBarItem(3, "Job Definitions", "/internal/jobdefinitions"));
    }
}
