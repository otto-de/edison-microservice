package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.ApplicationStatus;

/**
 * Aggregates the ApplicationStatus from all registered StatusDetailIndicators.
 *
 * @author Guido Steinacker
 * @since 13.02.15
 */
public interface ApplicationStatusAggregator {

    /**
     * Aggregate and return the current {@link ApplicationStatus} from all
     * registered {@link de.otto.edison.status.indicator.StatusDetailIndicator}s.
     *
     * @return aggregated ApplicationStatus
     */
    public ApplicationStatus aggregatedStatus();

    /**
     * Optionally implement an update method, that is used to explicitly update a
     * cached ApplicationStatus. This is used by the {@link de.otto.edison.status.scheduler.Scheduler}.
     */
    public default void update() {}

}
