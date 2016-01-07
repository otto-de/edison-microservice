package de.otto.edison.status.domain;

import de.otto.edison.annotations.Beta;

/**
 * Non-functional requirements regarding the availability of something this service is depending on.
 *
 */
@Beta
public enum AvailabilityRequirement {
    HIGH, MEDIUM, LOW
}
