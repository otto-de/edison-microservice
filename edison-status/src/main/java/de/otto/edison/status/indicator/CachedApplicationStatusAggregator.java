package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private final List<StatusDetailIndicator> indicators;

    public CachedApplicationStatusAggregator(final ApplicationInfo applicationInfo,
                                             final SystemInfo systemInfo,
                                             final VersionInfo versionInfo,
                                             final List<StatusDetailIndicator> indicators) {
        this.applicationInfo = applicationInfo;
        this.systemInfo = systemInfo;
        this.versionInfo = versionInfo;
        this.indicators = indicators;
        this.cachedStatus = applicationStatus(applicationInfo, systemInfo, versionInfo, Collections.<StatusDetail>emptyList());
    }

    @Override
    public ApplicationStatus aggregatedStatus() {
        return cachedStatus;
    }

    @Override
    public void update() {
        final List<StatusDetail> allDetails = indicators
                .stream()
                .flatMap(i->i.statusDetails().stream())
                .collect(toList());
        this.cachedStatus = applicationStatus(applicationInfo, systemInfo, versionInfo, allDetails);
    }

}
