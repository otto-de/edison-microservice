package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.StatusDetail;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Indicates status information about a single part of the application.
 *
 * The status of the application is calculated using one or more StatusDetails. StatusDetailIndicators
 * are used to calculate and offer StatusDetails.
 *
 * @author Guido Steinacker
 * @since 13.02.15
 */
public interface StatusDetailIndicator {

    /**
     * Return multiple StatusDetails about different parts of the application.
     *
     * @return list of StatusDetails
     */
    List<StatusDetail> statusDetails();

}
