package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;

import java.util.List;
import java.util.stream.Stream;

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
    private final StatusDetailSinceCache statusSinceCache;

    public CachedApplicationStatusAggregator(final ApplicationStatus applicationStatus,
                                             final List<StatusDetailIndicator> indicators) {
        this(applicationStatus, indicators, new StatusDetailSinceCache());
    }

    public CachedApplicationStatusAggregator(final ApplicationStatus applicationStatus,
                                             final List<StatusDetailIndicator> indicators,
                                             final StatusDetailSinceCache statusSinceCache) {
        this.cachedStatus = applicationStatus;
        this.indicators = indicators;
        this.statusSinceCache = statusSinceCache;
    }

    @Override
    public ApplicationStatus aggregatedStatus() {
        return cachedStatus;
    }

    @Override
    public void update() {
        final List<StatusDetail> statusDetails = indicators
                .stream()
                .flatMap(i -> {
                    try {
                        return i.statusDetails().stream();
                    } catch (RuntimeException e) {
                        return Stream.of(StatusDetail.statusDetail(i.getClass().getSimpleName(), Status.ERROR, "got exception: " + e.getLocalizedMessage()));
                    }
                })
                .collect(toList());
        cachedStatus = applicationStatus(
                cachedStatus.application,
                cachedStatus.cluster,
                cachedStatus.system,
                cachedStatus.vcs,
                cachedStatus.team,
                statusSinceCache.withSince(statusDetails));
    }

}
