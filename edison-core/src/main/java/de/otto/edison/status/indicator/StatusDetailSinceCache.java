package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import net.jcip.annotations.ThreadSafe;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Keeps track of the point in time a {@link StatusDetail} first reported its current {@link Status}.
 *
 * StatusDetails are immutable and are recreated by their {@link StatusDetailIndicator} on every update,
 * so the information "how long has this been in this state" cannot live in the StatusDetail itself.
 * This cache stores the timestamp per StatusDetail name - which is unique - and enriches the freshly
 * created StatusDetails with it via {@link StatusDetail#withSince(Instant)}.
 *
 * The timestamp is only replaced when the Status of a StatusDetail changes. Changes of the message,
 * the links or the additional details do not reset it.
 *
 * StatusDetails that disappear from the aggregation are evicted, so an indicator that is removed and
 * later comes back starts with a fresh timestamp.
 *
 * @see CachedApplicationStatusAggregator
 */
@ThreadSafe
public class StatusDetailSinceCache {

    private final Clock clock;
    private final Map<String, StatusSince> cache = new ConcurrentHashMap<>();

    public StatusDetailSinceCache() {
        this(Clock.systemUTC());
    }

    public StatusDetailSinceCache(final Clock clock) {
        this.clock = requireNonNull(clock, "Clock must not be null");
    }

    /**
     * Returns copies of the given StatusDetails, each one carrying the timestamp of the first occurrence
     * of its current Status.
     *
     * @param statusDetails the freshly created StatusDetails
     * @return the StatusDetails including their {@link StatusDetail#getSince() since} timestamp
     */
    public List<StatusDetail> withSince(final List<StatusDetail> statusDetails) {
        final Instant now = clock.instant();
        final List<StatusDetail> result = statusDetails
                .stream()
                .map(statusDetail -> statusDetail.withSince(sinceOf(statusDetail, now)))
                .collect(toList());
        evictDisappearedStatusDetails(statusDetails);
        return result;
    }

    /**
     * Removes all cached timestamps.
     */
    public void clear() {
        cache.clear();
    }

    private Instant sinceOf(final StatusDetail statusDetail, final Instant now) {
        return cache.compute(statusDetail.getName(), (name, cached) ->
                cached != null && cached.status() == statusDetail.getStatus()
                        ? cached
                        : new StatusSince(statusDetail.getStatus(), now)
        ).since();
    }

    private void evictDisappearedStatusDetails(final List<StatusDetail> statusDetails) {
        final Set<String> currentNames = new HashSet<>();
        statusDetails.forEach(statusDetail -> currentNames.add(statusDetail.getName()));
        cache.keySet().retainAll(currentNames);
    }

    private record StatusSince(Status status, Instant since) {}
}
