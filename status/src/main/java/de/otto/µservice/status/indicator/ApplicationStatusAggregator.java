package de.otto.µservice.status.indicator;

import de.otto.µservice.status.domain.ApplicationStatus;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
public interface ApplicationStatusAggregator {

    public ApplicationStatus aggregate();

}
