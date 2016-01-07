package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.ApplicationInfo;
import de.otto.edison.status.domain.SystemInfo;
import de.otto.edison.status.domain.VersionInfo;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import de.otto.edison.status.indicator.CachedApplicationStatusAggregator;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;


/**
 * Configuration of the default ApplicationStatusAggregator that is used to get and cache the status of this
 * application using all StatusDetailIndicators configured in the Spring application context.
 *
 */
@Configuration
public class StatusConfiguration {

    @Autowired(required = false)
    private List<StatusDetailIndicator> statusDetailIndicators = emptyList();

    /**
     * By default, a CachedApplicationStatusAggregator is used. The status is updated using a
     * {@link de.otto.edison.status.scheduler.Scheduler} every now and then.
     *
     * @param applicationInfo Information about the application
     * @param versionInfo Information about the application's version
     * @param systemInfo Information about the system
     * @return ApplicationStatusAggregator
     */
    @Bean
    @ConditionalOnMissingBean(ApplicationStatusAggregator.class)
    public ApplicationStatusAggregator applicationStatusAggregator(final ApplicationInfo applicationInfo,
                                                                   final VersionInfo versionInfo,
                                                                   final SystemInfo systemInfo) {
        final List<StatusDetailIndicator> indicators = statusDetailIndicators != null
                ? statusDetailIndicators
                : emptyList();
        return new CachedApplicationStatusAggregator(applicationInfo, systemInfo, versionInfo, indicators);
    }

}
