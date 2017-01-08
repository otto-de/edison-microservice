package de.otto.edison.status.domain;

import de.otto.edison.annotations.Beta;

/**
 * Non-functional requirements regarding the performance of something this service is depending on.
 *
 */
@Beta
public enum Criticality {
    /** If not available, this service is unable to operate. */
    MISSION_CRITICAL,
    /** If not available, the service will continue to operate, but we are loosing money / serving less features */
    BUSINESS_CRITICAL,
    /** If not available, we will not have too much trouble in the near future. */
    FUNCTIONAL_CRITICAL,
    /** Less important stuff. */
    NON_CRITICAL,
    /** Default value if not criticality was specified. */
    NOT_SPECIFIED
}
