package de.otto.edison.navigation.configuration;

import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.navigation.NavBar;
import de.otto.edison.navigation.NavBarItem;
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
@EnableConfigurationProperties(EdisonApplicationProperties.class)
public class NavBarConfiguration {

    public static final String MAIN_NAV_BAR = "mainNavBar";
    public static final String RIGHT_NAV_BAR = "rightNavBar";

    @Bean(name = MAIN_NAV_BAR)
    public NavBar mainNavBar() {
        return emptyNavBar();
    }

    @Bean(name = RIGHT_NAV_BAR)
    public NavBar rightNavBar(final EdisonApplicationProperties properties) {
        final String href = properties.getManagement().getBasePath() + "/status";
        return navBar(asList(
                navBarItem(top(), "Status", href)
        ));
    }
}
