package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.TeamInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by guido on 11.01.16.
 */
@Configuration
public class TeamInfoConfiguration {


    @Value("${edison.status.team.name:}")
    private String name;
    @Value("${edison.status.team.technical-contact:}")
    private String technicalContact;
    @Value("${edison.status.team.business-contact:}")
    private String businessContact;

    @Bean
    @ConditionalOnMissingBean(TeamInfo.class)
    public TeamInfo teamInfo() {
        return TeamInfo.teamInfo(name, technicalContact, businessContact);
    }
}
