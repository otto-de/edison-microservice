package de.otto.edison.acceptance.status;

import de.otto.edison.status.domain.ApplicationInfo;
import de.otto.edison.status.domain.Criticality;
import de.otto.edison.status.domain.Level;
import de.otto.edison.status.domain.ServiceDependency;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.TeamInfo;
import de.otto.edison.status.domain.VersionInfo;
import de.otto.edison.status.indicator.MutableStatusDetailIndicator;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.configuration.EdisonApplicationProperties.edisonApplicationProperties;
import static de.otto.edison.status.configuration.TeamInfoProperties.teamInfoProperties;
import static de.otto.edison.status.configuration.VersionInfoProperties.versionInfoProperties;
import static de.otto.edison.status.domain.ServiceDependencyBuilder.restServiceDependency;
import static de.otto.edison.status.domain.StatusDetail.statusDetail;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
@Configuration
public class StatusAcceptanceConfiguration {

    // meta infos:

    @Bean
    ApplicationInfo applicationInfo() {
        return ApplicationInfo.applicationInfo("test-app", edisonApplicationProperties("Some Test", "test-group", "test-env", "desc"));
    }

    @Bean
    TeamInfo teamInfo() {
        return TeamInfo.teamInfo(teamInfoProperties("Test Team", "technical@example.org", "business@example.org"));
    }

    @Bean
    VersionInfo versionInfo() {
        return VersionInfo.versionInfo(versionInfoProperties("1.0.0", "ab1234", "http://example.org/vcs/{version}"));
    }

    // some statusDetailIndicators:

    @Bean
    StatusDetailIndicator fooIndicator() {
        return new MutableStatusDetailIndicator(statusDetail("foo", Status.OK, "test ok"));
    }

    @Bean
    StatusDetailIndicator barIndicator() {
        return new MutableStatusDetailIndicator(statusDetail("bar", Status.WARNING, "test warning"));
    }

    @Bean
    Criticality criticality() {
        return Criticality.criticality(Level.LOW, "some impact");
    }

    @Bean
    ServiceDependency someDependency() {
        return restServiceDependency("http://example.com/foo").build();
    }
}
