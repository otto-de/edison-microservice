package de.otto.edison.navigation;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.navigation.NavBar.emptyNavBar;
import static de.otto.edison.navigation.NavBar.navBar;
import static de.otto.edison.navigation.NavBarItem.navBarItem;
import static de.otto.edison.navigation.NavBarItem.top;
import static java.util.Arrays.asList;

/**
 * Configuration of the {@link NavBar}s of Edison Microservices.
 * <p>
 *     You can add additional {@link NavBarItem NavBarItems} by calling {@link NavBar#register(NavBarItem)}.
 * </p>
 * <pre><code>
 *    {@literal @}Autowired
 *     private NavBar rightNavBar;
 *
 *    {@literal @}PostConstruct
 *     public void postConstruct() {
 *         rightNavBar.register(
 *                 navBarItem(bottom(), "My Page", "/my/page")
 *         );
 *     }
 * </code></pre>
 *
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(WebEndpointProperties.class)
public class NavBarConfiguration {

    @Bean
    public NavBar mainNavBar() {
        return emptyNavBar();
    }

    @Bean
    public NavBar rightNavBar(final WebEndpointProperties webEndpointProperties) {
        return navBar(asList(
                navBarItem(top(), "Status", webEndpointProperties.getBasePath() + "/status")
        ));
    }
}
