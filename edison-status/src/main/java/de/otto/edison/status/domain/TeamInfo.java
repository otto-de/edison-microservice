package de.otto.edison.status.domain;

import de.otto.edison.annotations.Beta;

/**
 * Information about the team that is responsible for developing and running the service.
 *
 * (yes, it should always be one team beeing responsible).
 */
@Beta
public final class TeamInfo {

    /** The name of the team. */
    public final String name;
    /** A technical contact like, for example, a phone number or mail address. */
    public final String technicalContact;
    /** A business contact like, for example, a phone number or mail address. */
    public final String businessContact;

    public TeamInfo(final String name,
                    final String technicalContact,
                    final String businessContact) {
        this.name = name;
        this.technicalContact = technicalContact;
        this.businessContact = businessContact;
    }
}
