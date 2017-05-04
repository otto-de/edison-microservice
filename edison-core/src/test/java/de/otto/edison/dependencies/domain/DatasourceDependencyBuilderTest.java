package de.otto.edison.dependencies.domain;

import org.junit.Test;

import static de.otto.edison.dependencies.domain.Datasource.datasource;
import static de.otto.edison.dependencies.domain.DatasourceDependencyBuilder.*;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class DatasourceDependencyBuilderTest {

    @Test
    public void shouldBuildDatasourceDependency() {
        final DatasourceDependency dependency = datasourceDependency(datasource("foo:42/bar"))
                .withType("test")
                .withSubtype("unittest")
                .withName("name")
                .withDescription("description")
                .withGroup("search")
                .build();
        assertThat(dependency.name).isEqualTo("name");
        assertThat(dependency.description).isEqualTo("description");
        assertThat(dependency.group).isEqualTo("search");
        assertThat(dependency.type).isEqualTo("test");
        assertThat(dependency.subtype).isEqualTo("unittest");
        assertThat(dependency.datasources).contains(datasource("foo", 42, "bar"));
    }

    @Test
    public void shouldCopyDatasource() {
        DatasourceDependency dependency = mongoDependency(singletonList(datasource("foo"))).build();
        assertThat(dependency).isEqualTo(copyOf(dependency).build());
        assertThat(dependency.hashCode()).isEqualTo(copyOf(dependency).build().hashCode());
    }

    @Test
    public void shouldBuildMongoDatasource() {
        DatasourceDependency dependency = mongoDependency(singletonList(datasource("foo"))).build();
        assertThat(dependency.datasources).contains(datasource("foo", -1, ""));
        assertThat(dependency.type).isEqualTo("db");
        assertThat(dependency.subtype).isEqualTo("MongoDB");
    }

    @Test
    public void shouldBuildCassandraDatasource() {
        DatasourceDependency dependency = cassandraDependency(singletonList(datasource("foo"))).build();
        assertThat(dependency.datasources).contains(datasource("foo", -1, ""));
        assertThat(dependency.type).isEqualTo("db");
        assertThat(dependency.subtype).isEqualTo("Cassandra");
    }

    @Test
    public void shouldBuildRedisDatasource() {
        DatasourceDependency dependency = redisDependency(singletonList(datasource("foo"))).build();
        assertThat(dependency.datasources).contains(datasource("foo", -1, ""));
        assertThat(dependency.type).isEqualTo("db");
        assertThat(dependency.subtype).isEqualTo("Redis");
    }

    @Test
    public void shouldBuildKafkaDatasource() {
        DatasourceDependency dependency = kafkaDependency(singletonList(datasource("foo"))).build();
        assertThat(dependency.datasources).contains(datasource("foo", -1, ""));
        assertThat(dependency.type).isEqualTo("queue");
        assertThat(dependency.subtype).isEqualTo("Kafka");
    }



}