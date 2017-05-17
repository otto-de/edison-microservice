package de.otto.edison.status.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static de.otto.edison.status.domain.Datasource.datasource;
import static de.otto.edison.status.domain.DatasourceDependencyBuilder.mongoDependency;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class DatasourceDependencyTest {

    @Test
    public void shouldTransformToJson() throws JsonProcessingException {
        final DatasourceDependency dependency = someMongoDb();
        final String json = new ObjectMapper().writeValueAsString(dependency);
        assertThat(json).isEqualTo("{" +
                "\"name\":\"shoppingcart-db\"," +
                "\"description\":\"Shoppingcart Database\"," +
                "\"type\":\"db\"," +
                "\"subtype\":\"MongoDB\"," +
                "\"criticality\":{\"level\":\"NOT_SPECIFIED\",\"disasterImpact\":\"Not Specified\"}," +
                "\"expectations\":{\"availability\":\"NOT_SPECIFIED\",\"performance\":\"NOT_SPECIFIED\"}," +
                "\"datasources\":[\"10.42.42.41:27001/shoppingcarts\",\"10.42.42.42:27001/shoppingcarts\"]" +
                "}");
    }

    @Test
    public void shouldTransformFromJson() throws IOException {
        final String json = "{" +
                "\"name\":\"shoppingcart-db\"," +
                "\"description\":\"Shoppingcart Database\"," +
                "\"type\":\"db\"," +
                "\"subtype\":\"MongoDB\"," +
                "\"datasources\":[\"10.42.42.41:27001/shoppingcarts\",\"10.42.42.42:27001/shoppingcarts\"]" +
                "}";
        final DatasourceDependency dependency = new ObjectMapper().readValue(json, DatasourceDependency.class);
        final DatasourceDependency expected = someMongoDb();
        assertThat(dependency).isEqualTo(expected);
    }

    @Test
    public void shouldIgnoreNullValues() throws JsonProcessingException {
        final DatasourceDependency dependency = new DatasourceDependency(null, null, "", "", emptyList(), null, null);
        final String json = new ObjectMapper().writeValueAsString(dependency);
        assertThat(json).isEqualTo("{\"name\":\"\",\"description\":\"\",\"type\":\"\",\"subtype\":\"\",\"criticality\":{\"level\":\"NOT_SPECIFIED\",\"disasterImpact\":\"Not Specified\"},\"expectations\":{\"availability\":\"NOT_SPECIFIED\",\"performance\":\"NOT_SPECIFIED\"},\"datasources\":[]}");
    }

    @Test
    public void shouldBeEqual() {
        assertThat(someMongoDb()).isEqualTo(someMongoDb());
    }

    @Test
    public void shouldHaveSameHashCode() {
        assertThat(someMongoDb().hashCode()).isEqualTo(someMongoDb().hashCode());
    }

    private DatasourceDependency someMongoDb() {
        return mongoDependency(asList(
                datasource("10.42.42.41:27001/shoppingcarts"),
                        datasource("10.42.42.42:27001/shoppingcarts")))
                .withName("shoppingcart-db")
                .withDescription("Shoppingcart Database")
                .build();
    }
}