package de.otto.edison.status.domain;

import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

@Beta
@Immutable
public class DatasourceDependency extends ExternalDependency {
    public static final String TYPE_DB = "db";
    public static final String TYPE_QUEUE = "queue";

    public static final String SUBTYPE_CASSANDRA = "Cassandra";
    public static final String SUBTYPE_MONGODB = "MongoDB";
    public static final String SUBTYPE_REDIS = "Redis";
    public static final String SUBTYPE_ELASTICSEARCH = "ElasticSearch";
    public static final String SUBTYPE_KAFKA = "Kafka";

    private final List<Datasource> datasources;

    DatasourceDependency() {
        this(null, null, null, "", "", emptyList(), null, null);
    }

    public DatasourceDependency(final String name,
                                final String group,
                                final String description,
                                final String type,
                                final String subtype,
                                final List<Datasource> datasources,
                                final Criticality criticality,
                                final Expectations expectations) {
        super(name, group, description, type, subtype, criticality, expectations);
        this.datasources = requireNonNull(datasources, "Parameter 'datasources' must not be null");
    }

    /**
     * DataSource descriptors for databases or queues
     *
     * @return list of datasources
     */
    public List<Datasource> getDatasources() {
        return datasources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatasourceDependency)) return false;
        if (!super.equals(o)) return false;
        DatasourceDependency that = (DatasourceDependency) o;
        return Objects.equals(getDatasources(), that.getDatasources());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDatasources());
    }

    @Override
    public String toString() {
        return "DatasourceDependency{" +
                "datasources=" + getDatasources() +
                "} " + super.toString();
    }
}
