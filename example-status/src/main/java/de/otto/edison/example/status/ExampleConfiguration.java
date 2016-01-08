package de.otto.edison.example.status;

import de.otto.edison.about.spec.TeamInfo;
import de.otto.edison.status.domain.VersionInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by guido on 08.01.16.
 */
@Configuration
public class ExampleConfiguration {

    @Bean
    TeamInfo teamInfo() {
        return TeamInfo.teamInfo("Edison Team", "edison-dev@example.org", "edison-team@example.org");
    }

    @Bean
    VersionInfo versionInfo() {
        return VersionInfo.versionInfo("pre 1.0.0", "", "https://github.com/otto-de/edison-microservice");
    }
}
