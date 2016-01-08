package de.otto.edison.acceptance.about;

import de.otto.edison.about.spec.*;
import de.otto.edison.status.domain.VersionInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    ServiceSpec fooTestService() {
        return ServiceSpec.serviceSpec("Foo Test", ServiceType.OTHER, "http://example.org/foo");
    }

    @Bean
    ServiceSpec barTestService() {
        return ServiceSpec.serviceSpec("Bar Test", ServiceType.OTHER, "http://example.org/bar", AvailabilityRequirement.LOW, PerformanceRequirement.LOW);
    }


}
