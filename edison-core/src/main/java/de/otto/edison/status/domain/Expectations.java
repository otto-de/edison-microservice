package de.otto.edison.status.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static de.otto.edison.status.domain.Availability.HIGH;
import static de.otto.edison.status.domain.Availability.LOW;
import static de.otto.edison.status.domain.Availability.MEDIUM;
import static de.otto.edison.status.domain.Availability.NOT_SPECIFIED;

/**
 * Describes expectations about an external dependency.
 *
 * @since 1.0.0
 */
@Beta
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public final class Expectations {
    public final Availability availability;
    public final Performance performance;

    /**
     * Factory method returning Expections with unspecified availabilty and performance requirements.
     *
     * @return unspecified expectations
     */
    public static Expectations unspecifiedExpectations() {
        return new Expectations(NOT_SPECIFIED, Performance.NOT_SPECIFIED);
    }

    /**
     * Factory method returning Expections with low availabilty and performance requirements.
     *
     * @return low expectations
     */
    public static Expectations lowExpectations() {
        return new Expectations(LOW, Performance.LOW);
    }

    /**
     * Factory method returning Expections with medium availabilty and performance requirements.
     *
     * @return medium expectations
     */
    public static Expectations mediumExpectations() {
        return new Expectations(MEDIUM, Performance.MEDIUM);
    }

    /**
     * Factory method returning Expections with high availabilty and performance requirements.
     *
     * @return high expectations
     */
    public static Expectations highExpectations() {
        return new Expectations(HIGH, Performance.HIGH);
    }

    /**
     * Factory method returning Expections with given availabilty and performance requirements.
     *
     * @param availability the expected availability
     * @param performance  the expected performance
     * @return Expectations
     */
    public static Expectations expects(final Availability availability,
                                       final Performance performance) {
        return new Expectations(availability, performance);
    }

    private Expectations() {
        this(null, null);
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
