package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.ApplicationStatus;

import java.util.List;

import static de.otto.edison.status.domain.ApplicationStatus.applicationStatus;
import static java.util.stream.Collectors.toList;

/**
 * A caching ApplicationStatusAggregator.
 *
 * @author Guido Steinacker
 * @since 13.02.15
 */
public class CachedApplicationStatusAggregator implements ApplicationStatusAggregator {

    private volatile ApplicationStatus cachedStatus;

    private final List<StatusDetailIndicator> indicators;

    public CachedApplicationStatusAggregator(final ApplicationStatus applicationStatus,
                                             final List<StatusDetailIndicator> indicators) {
        this.cachedStatus = applicationStatus;
        this.indicators = indicators;
    }

    @Override
    public ApplicationStatus aggregatedStatus() {
        return cachedStatus;
    }

    @Override
    public void update() {
        cachedStatus = applicationStatus(
                cachedStatus.application,
                cachedStatus.cluster,
                cachedStatus.system,
                cachedStatus.vcs,
                cachedStatus.team,
                indicators
                        .stream()
                        .flatMap(i -> i.statusDetails().stream())
                        .collect(toList()));
    }

}
