package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.ApplicationInfo;
import de.otto.edison.status.domain.ClusterInfo;
import de.otto.edison.status.domain.SystemInfo;
import de.otto.edison.status.domain.TeamInfo;
import de.otto.edison.status.domain.VersionInfo;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import de.otto.edison.status.indicator.CachedApplicationStatusAggregator;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import de.otto.edison.status.scheduler.CronScheduler;
import de.otto.edison.status.scheduler.EveryTenSecondsScheduler;
import de.otto.edison.status.scheduler.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

import static de.otto.edison.status.domain.ApplicationStatus.applicationStatus;
import static java.util.Collections.emptyList;

/**
 * Configuration of the default ApplicationStatusAggregator that is used to get and cache the status of this
 * application using all StatusDetailIndicators configured in the Spring application context.
 */
@Configuration
@EnableScheduling
public class ApplicationStatusAggregatorConfiguration {

    @Autowired(required = false)
    private List<StatusDetailIndicator> statusDetailIndicators = emptyList();

    @Autowired(required = false)
    private ClusterInfo clusterInfo;

    /**
     * By default, a CachedApplicationStatusAggregator is used. The status is updated using a
     * {@link de.otto.edison.status.scheduler.Scheduler} every now and then.
     *
     * @param applicationInfo Information about the application
     * @param versionInfo     Information about the application's version
     * @param systemInfo      Information about the system
     * @param teamInfo        Information about the team responsible for this application
     * @return ApplicationStatusAggregator
     */
    @Bean
    @ConditionalOnMissingBean(ApplicationStatusAggregator.class)
    public ApplicationStatusAggregator applicationStatusAggregator(final ApplicationInfo applicationInfo,
                                                                   final VersionInfo versionInfo,
                                                                   final SystemInfo systemInfo,
                                                                   final TeamInfo teamInfo) {
        final List<StatusDetailIndicator> indicators = statusDetailIndicators != null
                ? statusDetailIndicators
                : emptyList();
        return new CachedApplicationStatusAggregator(
                applicationStatus(applicationInfo, clusterInfo, systemInfo, versionInfo, teamInfo, emptyList()),
                indicators);
    }

    /**
     * Cron scheduler that updating the status using a cron expression.
     *
     * This is used if edison.status.scheduler.cron is configured.
     *
     * @param applicationStatusAggregator the ApplicationStatusAggregator scheduled for updates
     * @return CronScheduler a cron scheduler
     */
    @Bean
    @ConditionalOnProperty(name = "edison.status.scheduler.cron")
    public Scheduler cronScheduler(final ApplicationStatusAggregator applicationStatusAggregator) {
        return new CronScheduler(
                applicationStatusAggregator
        );
    }

    /**
     * Scheduler that is updating the status every ten seconds.
     *
     * This is used by default, if no other scheduler is configured.
     *
     * @param applicationStatusAggregator the ApplicationStatusAggregator scheduled for updates
     * @return Scheduler fixed delay scheduler
     */
    @Bean
    @ConditionalOnMissingBean(Scheduler.class)
    public Scheduler fixedDelayScheduler(final ApplicationStatusAggregator applicationStatusAggregator) {
        return new EveryTenSecondsScheduler(
                applicationStatusAggregator
        );
    }
}
