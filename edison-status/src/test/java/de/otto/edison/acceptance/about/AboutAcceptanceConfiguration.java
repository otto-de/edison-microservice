package de.otto.edison.acceptance.about;

import de.otto.edison.about.spec.ServiceSpec;
import de.otto.edison.about.spec.TeamInfo;
import de.otto.edison.status.domain.ApplicationInfo;
import de.otto.edison.status.domain.VersionInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.about.spec.Criticality.MISSION_CRITICAL;
import static de.otto.edison.about.spec.Expectations.highExpectations;
import static de.otto.edison.about.spec.ServiceSpec.serviceSpec;
import static de.otto.edison.about.spec.ServiceType.serviceType;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
@Configuration
public class AboutAcceptanceConfiguration {

    @Bean
    TeamInfo teamInfo() {
        return TeamInfo.teamInfo("Test Team", "technical@example.org", "business@example.org");
    }

    @Bean
    VersionInfo versionInfo() {
        return VersionInfo.versionInfo("1.0.0", "ab1234", "http://example.org/vcs/{version}");
    }

    @Bean
    ApplicationInfo applicationInfo() {
        return ApplicationInfo.applicationInfo("test-app", "desc", "test-group", "test-env");
    }
    @Bean
    ServiceSpec fooTestService() {
        return serviceSpec("/test/foo", "fooTest", "http://example.org/foo");
    }

    @Bean
    ServiceSpec barTestService() {
        return serviceSpec("/test/bar", "BarTest", "http://example.org/bar", serviceType("TEST", MISSION_CRITICAL, "test will fail"), highExpectations());
    }


}
