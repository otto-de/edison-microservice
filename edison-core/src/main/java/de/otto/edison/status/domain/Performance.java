package de.otto.edison.status.domain;

import de.otto.edison.annotations.Beta;

/**
 * Non-functional requirements regarding the performance of something this service is depending on.
 *
 */
@Beta
public enum Performance {
    /**
     * Depending on your overall requirements, this might be something like &lt; 10ms in the 99 percentile:
     */
    VERY_HIGH,
    /**
     * Depending on your overall requirements, this might be something like &lt; 50ms in the 99 percentile:
     */
    HIGH,
    /**
     * Depending on your overall requirements, this might be something like &lt; 100ms in the 99 percentile:
     */
    MEDIUM,
    /**
     * Depending on your overall requirements, this might be something like &lt; 1000ms in the 99 percentile:
     */
    LOW,
    /** Default value if no performance requirements where specified. */
    NOT_SPECIFIED
}
