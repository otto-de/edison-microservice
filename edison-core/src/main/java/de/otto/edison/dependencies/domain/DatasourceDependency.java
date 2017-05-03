package de.otto.edison.dependencies.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Beta
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class DatasourceDependency extends ExternalDependency {
    public static final String TYPE_DB = "db";
    public static final String TYPE_QUEUE = "queue";

    public static final String SUBTYPE_CASSANDRA = "Cassandra";
    public static final String SUBTYPE_MONGODB = "MongoDB";
    public static final String SUBTYPE_REDIS = "Redis";
    public static final String SUBTYPE_KAFKA = "Kafka";

    /**
     * DataSource descriptors for databases or queues
     */
    public final List<Datasource> datasources;

    DatasourceDependency() {
        this(null, null, null, null, null, null);
    }

    public DatasourceDependency(final String name,
                                final String group,
                                final String description,
                                final String type,
                                final String subType,
                                final List<Datasource> datasources) {
        super(name, group, description, type, subType);
        this.datasources = datasources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatasourceDependency)) return false;
        if (!super.equals(o)) return false;
        DatasourceDependency that = (DatasourceDependency) o;
        return Objects.equals(datasources, that.datasources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), datasources);
    }

    @Override
    public String toString() {
        return "DatasourceDependency{" +
                "datasources=" + datasources +
                "} " + super.toString();
    }
}
