package de.otto.edison.status.configuration;

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
@EnableScheduling
public class StatusConfiguration {

    @Autowired(required = false)
    private List<StatusDetailIndicator> statusDetailIndicators = emptyList();

    @Autowired
    private VersionInfo versionInfo;

    @Value("${edison.application.name}")
    private String applicationName;

    @Bean
    @ConditionalOnMissingBean(ApplicationStatusAggregator.class)
    public ApplicationStatusAggregator applicationStatusAggregator() {
        return new CachedApplicationStatusAggregator(applicationName, versionInfo, statusDetailIndicators);
    }

    @Bean
    @ConditionalOnProperty(name = "edison.status.scheduler.cron")
    public Scheduler cronScheduler() {
        return new CronScheduler(
                applicationStatusAggregator()
        );
    }

    @Bean
    @ConditionalOnMissingBean(Scheduler.class)
    public Scheduler fixedDelayScheduler() {
        return new EveryTenSecondsScheduler(
                applicationStatusAggregator()
        );
    }

}
