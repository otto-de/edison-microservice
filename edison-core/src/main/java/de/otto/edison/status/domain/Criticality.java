package de.otto.edison.status.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.annotations.Beta;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static de.otto.edison.status.domain.Level.HIGH;
import static de.otto.edison.status.domain.Level.LOW;
import static de.otto.edison.status.domain.Level.MEDIUM;

/**
 * Non-functional requirements regarding the performance of something this service is depending on.
 *
 */
@Beta
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public final class Criticality {
    /** If not available, this service is unable to operate. */
    public static final Criticality MISSION_CRITICAL = criticality(HIGH, "Mission Critical");
    /** If not available, the service will continue to operate, but we are loosing money / serving less features */
    public static final Criticality BUSINESS_CRITICAL = criticality(HIGH, "Business Critical");
    /** If not available, we will not have too much trouble in the near future. */
    public static final Criticality FUNCTIONAL_CRITICAL = criticality(MEDIUM, "Functional Critical");
    /** Less important stuff. */
    public static final Criticality NON_CRITICAL = criticality(LOW, "Non Critical");
    /** Default value if not criticality was specified. */
    public static final Criticality NOT_SPECIFIED = criticality(Level.NOT_SPECIFIED, "Not Specified");

    public final Level level;
    public final String disasterImpact;

    private Criticality() {
        this(null, null);
    }

    private Criticality(final Level level,
                        final String disasterImpact) {
        this.level = level;
        this.disasterImpact = disasterImpact;
    }

    public static Criticality unspecifiedCriticality() {
        return NOT_SPECIFIED;
    }

    public static Criticality nonCritical() {
        return NON_CRITICAL;
    }

    public static Criticality lowCriticality(final String disasterImpact) {
        return criticality(LOW, disasterImpact);
    }

    public static Criticality mediumCriticality(final String disasterImpact) {
        return criticality(MEDIUM, disasterImpact);
    }

    public static Criticality highCriticality(final String disasterImpact) {
        return criticality(HIGH, disasterImpact);
    }

    public static Criticality functionalCritical() {
        return FUNCTIONAL_CRITICAL;
    }

    public static Criticality businessCritical() {
        return BUSINESS_CRITICAL;
    }

    public static Criticality missionCritical() {
        return MISSION_CRITICAL;
    }

    public static Criticality criticality(final Level level,
                                          final String disasterImpact) {
        return new Criticality(level, disasterImpact);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Criticality)) return false;
        Criticality that = (Criticality) o;
        return level == that.level &&
                Objects.equals(disasterImpact, that.disasterImpact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, disasterImpact);
    }

    @Override
    public String toString() {
        return "Criticality{" +
                "level=" + level +
                ", disasterImpact='" + disasterImpact + '\'' +
                '}';
    }
}
