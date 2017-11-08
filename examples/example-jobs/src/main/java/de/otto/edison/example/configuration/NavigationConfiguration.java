package de.otto.edison.example.configuration;

import de.otto.edison.navigation.NavBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import static de.otto.edison.navigation.NavBarItem.navBarItem;

@Component
@EnableConfigurationProperties(WebEndpointProperties.class)
public class NavigationConfiguration {

    @Autowired
    public NavigationConfiguration(final NavBar mainNavBar,
                                   final WebEndpointProperties  webEndpointProperties) {
        mainNavBar.register(navBarItem(0, "Home", "/"));
        mainNavBar.register(navBarItem(1, "Status", String.format("%s/status", webEndpointProperties.getBasePath())));
        mainNavBar.register(navBarItem(2, "Job Overview", String.format("%s/jobs", webEndpointProperties.getBasePath())));
        mainNavBar.register(navBarItem(3, "Job Definitions", String.format("%s/jobdefinitions", webEndpointProperties.getBasePath())));
    }
}
