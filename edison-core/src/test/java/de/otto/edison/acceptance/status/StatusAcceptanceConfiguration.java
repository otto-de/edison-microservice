package de.otto.edison.acceptance.status;

import de.otto.edison.status.configuration.TeamInfoProperties;
import de.otto.edison.status.domain.*;
import de.otto.edison.status.indicator.MutableStatusDetailIndicator;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.status.configuration.ApplicationInfoProperties.applicationInfoProperties;
import static de.otto.edison.status.configuration.TeamInfoProperties.*;
import static de.otto.edison.status.configuration.VersionInfoProperties.versionInfoProperties;
import static de.otto.edison.status.domain.Criticality.MISSION_CRITICAL;
import static de.otto.edison.status.domain.Expectations.highExpectations;
import static de.otto.edison.status.domain.ServiceSpec.serviceSpec;
import static de.otto.edison.status.domain.ServiceType.serviceType;
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
        return ApplicationInfo.applicationInfo("test-app", applicationInfoProperties("Some Test", "test-group", "test-env", "desc"));
    }

    @Bean
    TeamInfo teamInfo() {
        return TeamInfo.teamInfo(teamInfoProperties("Test Team", "technical@example.org", "business@example.org"));
    }

    @Bean
    VersionInfo versionInfo() {
        return VersionInfo.versionInfo(versionInfoProperties("1.0.0", "ab1234", "http://example.org/vcs/{version}"));
    }

    // some serviceSpecs:

    @Bean
    ServiceSpec fooTestService() {
        return serviceSpec("fooTest", "http://example.org/foo");
    }

    @Bean
    ServiceSpec barTestService() {
        return serviceSpec("BarTest", "http://example.org/bar", serviceType("TEST", MISSION_CRITICAL, "test will fail"), highExpectations());
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

}
