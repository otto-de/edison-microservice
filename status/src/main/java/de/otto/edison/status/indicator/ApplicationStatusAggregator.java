package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.ApplicationStatus;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
public interface ApplicationStatusAggregator {

    public ApplicationStatus aggregate();

}
