package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.*;

import java.util.List;

import static de.otto.edison.status.domain.ApplicationStatus.applicationStatus;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * A caching ApplicationStatusAggregator.
 *
 * @author Guido Steinacker
 * @since 13.02.15
 */
public class CachedApplicationStatusAggregator implements ApplicationStatusAggregator {

    private volatile ApplicationStatus cachedStatus;

    private final ApplicationInfo applicationInfo;
    private final SystemInfo systemInfo;
    private final VersionInfo versionInfo;
    private final TeamInfo teamInfo;
    private final List<StatusDetailIndicator> indicators;
    private final List<ServiceSpec> serviceSpecs;

    public CachedApplicationStatusAggregator(final ApplicationInfo applicationInfo,
                                             final SystemInfo systemInfo,
                                             final VersionInfo versionInfo,
                                             final TeamInfo teamInfo,
                                             final List<StatusDetailIndicator> indicators,
                                             final List<ServiceSpec> serviceSpecs) {
        this.applicationInfo = applicationInfo;
        this.systemInfo = systemInfo;
        this.versionInfo = versionInfo;
        this.teamInfo = teamInfo;
        this.indicators = indicators;
        this.serviceSpecs = serviceSpecs;
        this.cachedStatus = applicationStatus(applicationInfo, systemInfo, versionInfo, teamInfo, emptyList(), serviceSpecs);
    }

    @Override
    public ApplicationStatus aggregatedStatus() {
        return cachedStatus;
    }

    @Override
    public void update() {
        this.cachedStatus = applicationStatus(applicationInfo, systemInfo, versionInfo, teamInfo, getStatusDetails(indicators), serviceSpecs);
    }

    private static List<StatusDetail> getStatusDetails(final List<StatusDetailIndicator> indicators) {
        return indicators
                    .stream()
                    .flatMap(i->i.statusDetails().stream())
                    .collect(toList());
    }

}
