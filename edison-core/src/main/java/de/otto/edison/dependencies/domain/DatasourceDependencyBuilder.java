package de.otto.edison.dependencies.domain;

import java.util.List;

import static java.util.Arrays.asList;

public class DatasourceDependencyBuilder {
    private String name;
    private String group;
    private String description;
    private String type;
    private String subtype;
    private List<Datasource> datasources;

    public static DatasourceDependencyBuilder copyOf(final DatasourceDependency other) {
        return new DatasourceDependencyBuilder()
                .withName(other.name)
                .withGroup(other.group)
                .withDescription(other.description)
                .withType(other.type)
                .withSubtype(other.subtype)
                .withDatasources(other.datasources);
    }

    /**
     * Creates a ServiceDependencyBuilder with type="db" and subtype="MongoDB".
     *
     * @param datasources the datasources of the accessed database.
     * @return builder used to configure MongoDB datasource dependencies
     */
    public static DatasourceDependencyBuilder mongoDependency(final List<Datasource> datasources) {
        return new DatasourceDependencyBuilder()
                .withDatasources(datasources)
                .withType(DatasourceDependency.TYPE_DB)
                .withSubtype(DatasourceDependency.SUBTYPE_MONGODB);
    }

    /**
     * Creates a ServiceDependencyBuilder with type="db" and subtype="MongoDB".
     *
     * @param datasources the datasources of the accessed database.
     * @return builder used to configure MongoDB datasource dependencies
     */
    public static DatasourceDependencyBuilder mongoDependency(final Datasource... datasources) {
        return mongoDependency(asList(datasources));
    }

    /**
     * Creates a ServiceDependencyBuilder with type="db" and subtype="Redis".
     *
     * @param datasources the datasources of the accessed database.
     * @return builder used to configure Redis datasource dependencies
     */
    public static DatasourceDependencyBuilder redisDependency(final List<Datasource> datasources) {
        return new DatasourceDependencyBuilder()
                .withDatasources(datasources)
                .withType(DatasourceDependency.TYPE_DB)
                .withSubtype(DatasourceDependency.SUBTYPE_REDIS);
    }

    /**
     * Creates a ServiceDependencyBuilder with type="db" and subtype="Redis".
            *
     * @param datasources the datasources of the accessed database.
     * @return builder used to configure Redis datasource dependencies
     */
    public static DatasourceDependencyBuilder redisDependency(final Datasource... datasources) {
        return redisDependency(asList(datasources));
    }

    /**
     * Creates a ServiceDependencyBuilder with type="db" and subtype="Cassandra".
     *
     * @param datasources the datasources of the accessed database.
     * @return builder used to configure Cassandra datasource dependencies
     */
    public static DatasourceDependencyBuilder cassandraDependency(final List<Datasource> datasources) {
        return new DatasourceDependencyBuilder()
                .withDatasources(datasources)
                .withType(DatasourceDependency.TYPE_DB)
                .withSubtype(DatasourceDependency.SUBTYPE_CASSANDRA);
    }

    /**
     * Creates a ServiceDependencyBuilder with type="db" and subtype="Cassandra".
     *
     * @param datasources the datasources of the accessed database.
     * @return builder used to configure Cassandra datasource dependencies
     */
    public static DatasourceDependencyBuilder cassandraDependency(final Datasource... datasources) {
        return cassandraDependency(asList(datasources));
    }

    /**
     * Creates a ServiceDependencyBuilder with type="queue" and subtype="Kafka".
     *
     * @param datasources the datasources of the accessed queue.
     * @return builder used to configure Kafka datasource dependencies
     */
    public static DatasourceDependencyBuilder kafkaDependency(final List<Datasource> datasources) {
        return new DatasourceDependencyBuilder()
                .withDatasources(datasources)
                .withType(DatasourceDependency.TYPE_QUEUE)
                .withSubtype(DatasourceDependency.SUBTYPE_KAFKA);
    }

    /**
     * Creates a ServiceDependencyBuilder with type="queue" and subtype="Kafka".
     *
     * @param datasources the datasources of the accessed queue.
     * @return builder used to configure Kafka datasource dependencies
     */
    public static DatasourceDependencyBuilder kafkaDependency(final Datasource... datasources) {
        return kafkaDependency(asList(datasources));
    }

    /**
     * Creates a generic DataSourceDependencyBuilder.
     *
     * @param datasources the datasources of the accessed database.
     * @return builder used to configure other datasource dependencies
     */
    public static DatasourceDependencyBuilder datasourceDependency(final List<Datasource> datasources) {
        return new DatasourceDependencyBuilder()
                .withDatasources(datasources);
    }
    /**
     * Creates a generic DataSourceDependencyBuilder.
     *
     * @param datasources the datasources of the accessed database.
     * @return builder used to configure other datasource dependencies
     */
    public static DatasourceDependencyBuilder datasourceDependency(final Datasource... datasources) {
        return datasourceDependency(asList(datasources));
    }

    /**
     * @param name The name of the dependent service.
     */
    public DatasourceDependencyBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * @param group The service group like, for example, the vertical aka SCS the service is belonging to.
     */
    public DatasourceDependencyBuilder withGroup(final String group) {
        this.group = group;
        return this;
    }

    /**
     * @param description A human readable description of the dependency.
     */
    public DatasourceDependencyBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    /**
     * @param type The type of the dependency: db, queue, service, ...
     */
    public DatasourceDependencyBuilder withType(final String type) {
        this.type = type;
        return this;
    }

    /**
     * @param subtype The sub-type of the dependency: Cassandra, MongoDB, Kafka, REST, ...
     */
    public DatasourceDependencyBuilder withSubtype(final String subtype) {
        this.subtype = subtype;
        return this;
    }

    /**
     * @param dataSources DataSource descriptors for databases or queues
     */
    private DatasourceDependencyBuilder withDatasources(final List<Datasource> dataSources) {
        this.datasources = dataSources;
        return this;
    }

    /**
     * Builds a ServiceDependency instance.
     *
     * @return service dependency
     */
    public DatasourceDependency build() {
        return new DatasourceDependency(name, group, description, type, subtype, datasources);
    }
}