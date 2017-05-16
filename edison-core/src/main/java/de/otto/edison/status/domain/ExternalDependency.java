package de.otto.edison.status.domain;

import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

import java.util.Objects;

import static de.otto.edison.status.domain.Criticality.unspecifiedCriticality;
import static de.otto.edison.status.domain.Expectations.unspecifiedExpectations;

/**
 * A dependency to an external system, like other microservices, databases or message queues.
 * <p>
 *     Information about external dependencies is available by the
 *     {@link de.otto.edison.status.controller.StatusController /internal/status} page or JSON document of the service.
 * </p>
 */
@Beta
@Immutable
public class ExternalDependency {
    private final String name;
    private final String group;
    private final String description;
    private final String type;
    private final String subtype;
    private final Criticality criticality;
    private final Expectations expectations;

    /**
     *
     * @param name The name of the dependent service or datasource
     * @param group The service group like, for example, the vertical aka SCS the service is belonging to.
     * @param description A human readable description of the dependency.
     * @param type The type of the dependency: db, queue, service, ...
     * @param subtype The sub-type of the dependency: Cassandra, MongoDB, Kafka, REST, ...
     * @param criticality Describes the criticality of the dependency for this service.
     * @param expectations Expectations of this service to the external dependency with respect to availability and performance.
     */
    protected ExternalDependency(final String name,
                                 final String group,
                                 final String description,
                                 final String type,
                                 final String subtype,
                                 final Criticality criticality,
                                 final Expectations expectations) {
        this.name = Objects.toString(name, "");
        this.group = Objects.toString(group, "");
        this.description = Objects.toString(description, "");
        this.type = Objects.requireNonNull(type, "Parameter 'type' must not be null");
        this.subtype = Objects.requireNonNull(subtype, "Parameter 'subtype' must not be null");
        this.criticality = criticality != null ? criticality : unspecifiedCriticality();
        this.expectations = expectations != null ? expectations : unspecifiedExpectations();
    }

    /**
     * @return The name of the dependent service.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The service group like, for example, the vertical aka SCS the service is belonging to.
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return A human readable description of the dependency.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The type of the dependency: db, queue, service, ...
     */
    public String getType() {
        return type;
    }

    /**
     * @return The sub-type of the dependency: Cassandra, MongoDB, Kafka, REST, ...
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * @return Describes the criticality of the dependency for this service.
     */
    public Criticality getCriticality() {
        return criticality;
    }

    /**
     * @return Expectations of this service to the external dependency with respect to availability and performance.
     */
    public Expectations getExpectations() {
        return expectations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalDependency)) return false;
        ExternalDependency that = (ExternalDependency) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getGroup(), that.getGroup()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getType(), that.getType()) &&
                Objects.equals(getSubtype(), that.getSubtype()) &&
                Objects.equals(getCriticality(), that.getCriticality()) &&
                Objects.equals(getExpectations(), that.getExpectations());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getGroup(), getDescription(), getType(), getSubtype(), getCriticality(), getExpectations());
    }

    @Override
    public String toString() {
        return "ExternalDependency{" +
                "name='" + getName() + '\'' +
                ", group='" + getGroup() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", type='" + getType() + '\'' +
                ", subtype='" + getSubtype() + '\'' +
                ", criticality=" + getCriticality() +
                ", expectations=" + getExpectations() +
                '}';
    }

}
