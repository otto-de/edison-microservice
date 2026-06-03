package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.TeamInfo;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Configuration of the TeamInfo.
 */
@AutoConfiguration
@EnableConfigurationProperties(TeamInfoProperties.class)
public class TeamInfoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TeamInfo.class)
    public TeamInfo teamInfo(final TeamInfoProperties teamInfoProperties) {
        return TeamInfo.teamInfo(teamInfoProperties);
    }
}
