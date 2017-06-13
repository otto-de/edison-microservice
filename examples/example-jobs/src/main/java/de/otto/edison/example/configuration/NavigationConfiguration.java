package de.otto.edison.example.configuration;

import de.otto.edison.navigation.NavBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import static de.otto.edison.navigation.NavBarItem.navBarItem;

@Component
@EnableConfigurationProperties(ManagementServerProperties.class)
public class NavigationConfiguration {

    @Autowired
    public NavigationConfiguration(final NavBar mainNavBar,
                                   final ManagementServerProperties managementServerProperties) {
        mainNavBar.register(navBarItem(0, "Home", "/"));
        mainNavBar.register(navBarItem(1, "Status", String.format("%s/status", managementServerProperties.getContextPath())));
        mainNavBar.register(navBarItem(2, "Job Overview", String.format("%s/internal/jobs", managementServerProperties.getContextPath())));
        mainNavBar.register(navBarItem(3, "Job Definitions", String.format("%s/jobdefinitions", managementServerProperties.getContextPath())));
    }
}
