package de.otto.edison.dependencies.domain;

import org.junit.Test;

import java.util.List;

import static de.otto.edison.dependencies.domain.Datasource.datasource;
import static de.otto.edison.dependencies.domain.Datasource.datasources;
import static org.assertj.core.api.Assertions.assertThat;

public class DatasourceTest {

    @Test
    public void shouldBuildDatasource() {
        final Datasource testee = datasource("foo", 42, "bar");
        assertThat(testee.node).isEqualTo("foo");
        assertThat(testee.port).isEqualTo(42);
        assertThat(testee.resource).isEqualTo("bar");
    }

    @Test
    public void shouldSerializeDatasource() {
        final Datasource testee = datasource("foo", 42, "bar");
        assertThat(testee.toString()).isEqualTo("foo:42/bar");
    }

    @Test
    public void shouldBuildDatasourceFromString() {
        final Datasource testee = datasource("foo:42/bar");
        assertThat(testee.node).isEqualTo("foo");
        assertThat(testee.port).isEqualTo(42);
        assertThat(testee.resource).isEqualTo("bar");
    }

    @Test
    public void shouldBuildDatasourceFromString2() {
        final Datasource testee = datasource("foo:42/bar:foobar/0815");
        assertThat(testee.node).isEqualTo("foo");
        assertThat(testee.port).isEqualTo(42);
        assertThat(testee.resource).isEqualTo("bar:foobar/0815");
    }

    @Test
    public void shouldBuildDatasourceFromStringWithoutPort() {
        final Datasource testee = datasource("foo/bar");
        assertThat(testee.node).isEqualTo("foo");
        assertThat(testee.port).isEqualTo(-1);
        assertThat(testee.resource).isEqualTo("bar");
    }

    @Test
    public void shouldBuildDatasourceFromStringWithoutPortAndResource() {
        final Datasource testee = datasource("foo");
        assertThat(testee.node).isEqualTo("foo");
        assertThat(testee.port).isEqualTo(-1);
        assertThat(testee.resource).isEqualTo("");
    }

    @Test
    public void shouldParseListOfDatasources() {
        final List<Datasource> testee = datasources("foo:42/bar,foobar");
        assertThat(testee.get(0).node).isEqualTo("foo");
        assertThat(testee.get(0).port).isEqualTo(42);
        assertThat(testee.get(0).resource).isEqualTo("bar");
        assertThat(testee.get(1).node).isEqualTo("foobar");
        assertThat(testee.get(1).port).isEqualTo(-1);
        assertThat(testee.get(1).resource).isEqualTo("");
    }
}