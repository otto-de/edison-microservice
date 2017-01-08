package de.otto.edison.status.domain;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Expectations that = (Expectations) o;

        if (availability != that.availability) return false;
        return performance == that.performance;

    }

    @Override
    public int hashCode() {
        int result = availability != null ? availability.hashCode() : 0;
        result = 31 * result + (performance != null ? performance.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Expectations{" +
                "availability=" + availability +
                ", performance=" + performance +
                '}';
    }
}
