package de.otto.edison.status.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

/**
 * Information about the team that is responsible for developing and running the service.
 *
 * (yes, it should always be one team being responsible).
 */
@Beta
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamInfo {

    /** The name of the team. */
    public final String name;
    /** A technical contact like, for example, a phone number or mail address. */
    public final String technicalContact;
    /** A business contact like, for example, a phone number or mail address. */
    public final String businessContact;

    private TeamInfo(final String name,
                     final String technicalContact,
                     final String businessContact) {
        this.name = name;
        this.technicalContact = technicalContact;
        this.businessContact = businessContact;
    }

    public static TeamInfo teamInfo(final String name,
                                    final String technicalContact,
                                    final String businessContact) {
        return new TeamInfo(name, technicalContact, businessContact);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamInfo teamInfo = (TeamInfo) o;

        if (name != null ? !name.equals(teamInfo.name) : teamInfo.name != null) return false;
        if (technicalContact != null ? !technicalContact.equals(teamInfo.technicalContact) : teamInfo.technicalContact != null)
            return false;
        return !(businessContact != null ? !businessContact.equals(teamInfo.businessContact) : teamInfo.businessContact != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (technicalContact != null ? technicalContact.hashCode() : 0);
        result = 31 * result + (businessContact != null ? businessContact.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TeamInfo{" +
                "name='" + name + '\'' +
                ", technicalContact='" + technicalContact + '\'' +
                ", businessContact='" + businessContact + '\'' +
                '}';
    }
}
