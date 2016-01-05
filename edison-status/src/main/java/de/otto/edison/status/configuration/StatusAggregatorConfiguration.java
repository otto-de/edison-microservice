package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.ApplicationInfo;
import de.otto.edison.status.domain.SystemInfo;
import de.otto.edison.status.domain.VersionInfo;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import de.otto.edison.status.indicator.CachedApplicationStatusAggregator;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import de.otto.edison.status.scheduler.CronScheduler;
import de.otto.edison.status.scheduler.EveryTenSecondsScheduler;
import de.otto.edison.status.scheduler.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

import static java.util.Collections.emptyList;

@Configuration
public class StatusAggregatorConfiguration {

    @Autowired(required = false)
    private List<StatusDetailIndicator> statusDetailIndicators = emptyList();

    @Bean
    @ConditionalOnMissingBean(ApplicationStatusAggregator.class)
    public ApplicationStatusAggregator applicationStatusAggregator(final ApplicationInfo applicationInfo,
                                                                   final VersionInfo versionInfo,
                                                                   final SystemInfo systemInfo) {
        return new CachedApplicationStatusAggregator(applicationInfo, systemInfo, versionInfo, statusDetailIndicators);
    }

}
