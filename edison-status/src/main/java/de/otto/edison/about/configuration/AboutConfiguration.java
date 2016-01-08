package de.otto.edison.about.configuration;

import de.otto.edison.about.spec.About;
import de.otto.edison.about.spec.ServiceSpec;
import de.otto.edison.about.spec.TeamInfo;
import de.otto.edison.annotations.Beta;
import de.otto.edison.status.domain.ApplicationInfo;
import de.otto.edison.status.domain.SystemInfo;
import de.otto.edison.status.domain.VersionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * Configuration of the 'about' Spring bean that is used to render /internal/about.
 *
 * This configuration expects the following Spring beans:
 * <ul>
 *     <li>ApplicationInfo: Information about the application itself</li>
 *     <li>VersionInfo: Information about the application's version</li>
 *     <li>SystemInfo: Information about the system this service is running on</li>
 * </ul>
 * In addition to this, information can be added by configuring the following beans:
 * <ul>
 *     <li>TeamInfo: Information about the team responsible for the application</li>
 *     <li>List&lt;ServiceSpec&gt;: One or more beans providing information about other services, this service is depending on</li>
 * </ul>
 */
@Beta
@Configuration
public class AboutConfiguration {

    @Autowired
    private ApplicationInfo applicationInfo;
    @Autowired
    private VersionInfo versionInfo;
    @Autowired
    private SystemInfo systemInfo;
    @Autowired(required = false)
    private TeamInfo teamInfo;
    @Autowired(required = false)
    private List<ServiceSpec> serviceSpecs;

    /**
     * Default configuration of SpringBean 'about' used to render /internal/about.
     *
     * @return About
     */
    @Bean
    @ConditionalOnMissingBean(About.class)
    public About about() {
        return About.about(applicationInfo, versionInfo, systemInfo, ofNullable(teamInfo), ofNullable(serviceSpecs));
    }

}
