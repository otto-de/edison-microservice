package de.otto.edison.about.spec;

import de.otto.edison.annotations.Beta;

/**
 * Non-functional requirements regarding the availability of something this service is depending on.
 *
 */
@Beta
public enum Availability {
    /**
     * Depending on your overall requirements, this might be something like 99.9% Availability
     * with MTTR &lt; 10min or something similar.
     */
    HIGH,
    /**
     * Depending on your overall requirements, this might be something like 99.0% Availability
     * with MTTR &lt; 120min or something similar.
     */
    MEDIUM,
    /**
     * Depending on your overall requirements, this might be something like 90.0% Availability
     * with MTTR &lt; 1d or something similar.
     */
    LOW,
    /** Default value if no availabilty requirements where specified. */
    NOT_SPECIFIED
}