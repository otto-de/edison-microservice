package de.otto.edison.dependencies.domain;

import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

import java.util.Objects;

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
    /**
     * The name of the dependent service.
     */
    public final String name;
    /**
     * The service group like, for example, the vertical aka SCS the service is belonging to.
     */
    public final String group;
    /**
     * A human readable description of the dependency.
     */
    public final String description;
    /**
     * The type of the dependency: db, queue, service, ...
     */
    public final String type;
    /**
     * The sub-type of the dependency: Cassandra, MongoDB, Kafka, REST, ...
     */
    public final String subtype;

    protected ExternalDependency(final String name,
                                 final String group,
                                 final String description,
                                 final String type,
                                 final String subtype) {
        this.name = name;
        this.group = group;
        this.description = description;
        this.type = type;
        this.subtype = subtype;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExternalDependency)) return false;
        ExternalDependency that = (ExternalDependency) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(group, that.group) &&
                Objects.equals(description, that.description) &&
                Objects.equals(type, that.type) &&
                Objects.equals(subtype, that.subtype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, group, description, type, subtype);
    }

    @Override
    public String toString() {
        return "ExternalDependency{" +
                "name='" + name + '\'' +
                ", group='" + group + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", subtype='" + subtype + '\'' +
                '}';
    }
}
