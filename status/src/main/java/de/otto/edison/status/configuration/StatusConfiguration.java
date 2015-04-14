package de.otto.edison.status.configuration;

import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import de.otto.edison.status.indicator.CachingApplicationStatusAggregator;
import de.otto.edison.status.indicator.DefaultApplicationStatusAggregator;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static de.otto.edison.status.domain.VersionInfo.versionInfo;
import static java.util.Collections.emptyList;

@Configuration
public class StatusConfiguration {

    @Autowired(required = false)
    private List<StatusDetailIndicator> statusDetailIndicators = emptyList();

    @Value("${application.name}")
    private String applicationName;

    @Value("${info.build.version:unknown}")
    private String version;
    @Value(("${info.build.commit:unknown}"))
    private String commit;

    @Bean
    @ConditionalOnMissingBean(ApplicationStatusAggregator.class)
    public ApplicationStatusAggregator statusAggregator() {
        return new CachingApplicationStatusAggregator(
                new DefaultApplicationStatusAggregator(applicationName, versionInfo(version, commit), statusDetailIndicators)
        );
    }

}
