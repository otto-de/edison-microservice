package de.otto.edison.about.spec;

import net.jcip.annotations.Immutable;

/**
 * Describes expectations about another service.
 *
 * Created by guido on 08.01.16.
 */
@Immutable
public class Expectations {
    public final Availability availability;
    public final Performance performance;

    public static Expectations unspecifiedExpectations() {
        return new Expectations(Availability.NOT_SPECIFIED, Performance.NOT_SPECIFIED);
    }

    public static Expectations lowExpectations() {
        return new Expectations(Availability.LOW, Performance.LOW);
    }

    public static Expectations highExpectations() {
        return new Expectations(Availability.HIGH, Performance.HIGH);
    }

    public static Expectations expects(final Availability availability,
                                       final Performance performance) {
        return new Expectations(availability, performance);
    }

    private Expectations(final Availability availability,
                         final Performance performance) {
        this.availability = availability;
        this.performance = performance;
    }
}
