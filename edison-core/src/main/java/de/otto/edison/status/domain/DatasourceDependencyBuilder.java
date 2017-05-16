package de.otto.edison.status.domain;

import de.otto.edison.annotations.Beta;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * A builder used to build {@link DatasourceDependency datasource dependencies}.
 *
 * @since 1.1.0
 */
@Beta
public class DatasourceDependencyBuilder {
    private String name;
    private String group;
    private String description;
    private String type;
    private String subtype;
    private List<Datasource> datasources;
    private Criticality criticality;
    private Expectations expectations;

    /**
     * Returns a builder instance that is initialized using a prototype DatasourceDependency.
     * <p>
     *     All values of the prototype are copied.
     * </p>
     * @param prototype the prototype dependency
     * @return DatasourceDependencyBuilder
     */
    public static DatasourceDependencyBuilder copyOf(final DatasourceDependency prototype) {
        return new DatasourceDependencyBuilder()
                .withName(prototype.getName())
                .withGroup(prototype.getGroup())
                .withDescription(prototype.getDescription())
                .withType(prototype.getType())
                .withSubtype(prototype.getSubtype())
                .withDatasources(prototype.getDatasources());
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
     * Creates a ServiceDependencyBuilder with type="db" and subtype="ElasticSearch".
     *
     * @param datasources the datasources of the accessed ES services.
     * @return builder used to configure ES datasource dependencies
     */
    public static DatasourceDependencyBuilder elasticSearchDependency(final List<Datasource> datasources) {
        return new DatasourceDependencyBuilder()
                .withDatasources(datasources)
                .withType(DatasourceDependency.TYPE_DB)
                .withSubtype(DatasourceDependency.SUBTYPE_ELASTICSEARCH);
    }

    /**
     * Creates a ServiceDependencyBuilder with type="db" and subtype="ElasticSearch".
     *
     * @param datasources the datasources of the accessed ES services.
     * @return builder used to configure ES datasource dependencies
     */
    public static DatasourceDependencyBuilder elasticSearchDependency(final Datasource... datasources) {
        return elasticSearchDependency(asList(datasources));
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
     * @return this
     */
    public DatasourceDependencyBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * @param group The service group like, for example, the vertical aka SCS the service is belonging to.
     * @return this
     */
    public DatasourceDependencyBuilder withGroup(final String group) {
        this.group = group;
        return this;
    }

    /**
     * @param description A human readable description of the dependency.
     * @return this
     */
    public DatasourceDependencyBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    /**
     * @param type The type of the dependency: db, queue, service, ...
     * @return this
     */
    public DatasourceDependencyBuilder withType(final String type) {
        this.type = type;
        return this;
    }

    /**
     * @param subtype The sub-type of the dependency: Cassandra, MongoDB, Kafka, REST, ...
     * @return this
     */
    public DatasourceDependencyBuilder withSubtype(final String subtype) {
        this.subtype = subtype;
        return this;
    }

    /**
     * @param dataSources DataSource descriptors for databases or queues
     * @return this
     */
    private DatasourceDependencyBuilder withDatasources(final List<Datasource> dataSources) {
        this.datasources = dataSources;
        return this;
    }


    /**
     * @param criticality The criticality of the dependency
     * @return this
     */
    public DatasourceDependencyBuilder withCriticality(final Criticality criticality) {
        this.criticality = criticality;
        return this;
    }

    /**
     * @param expectations The expectations of of the dependency
     * @return this
     */
    public DatasourceDependencyBuilder withExpectations(final Expectations expectations) {
        this.expectations = expectations;
        return this;
    }

    /**
     * Builds a ServiceDependency instance.
     *
     * @return service dependency
     */
    public DatasourceDependency build() {
        return new DatasourceDependency(name, group, description, type, subtype, datasources, criticality, expectations);
    }
}