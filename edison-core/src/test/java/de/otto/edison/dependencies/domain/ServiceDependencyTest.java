package de.otto.edison.dependencies.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static de.otto.edison.dependencies.domain.ServiceDependency.SUBTYPE_REST;
import static de.otto.edison.dependencies.domain.ServiceDependency.TYPE_SERVICE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class ServiceDependencyTest {

    @Test
    public void shouldTransformToJson() throws JsonProcessingException {
        final ServiceDependency dependency = someRestfulService();
        final String json = new ObjectMapper().writeValueAsString(dependency);
        assertThat(json).isEqualTo("{" +
                "\"name\":\"shoppingcart\"," +
                "\"group\":\"order\"," +
                "\"description\":" +
                "\"Imports shoppingcarts\"," +
                "\"type\":\"service\"," +
                "\"subType\":\"REST\"," +
                "\"url\":\"http://example.com/order/shoppingcarts\"," +
                "\"methods\":[\"GET\"]," +
                "\"mediaTypes\":[\"application/json\"]," +
                "\"authentication\":\"OAUTH\"" +
                "}");
    }

    @Test
    public void shouldTransformFromJson() throws IOException {
        final String json = "{" +
                "\"name\":\"shoppingcart\"," +
                "\"group\":\"order\"," +
                "\"description\":" +
                "\"Imports shoppingcarts\"," +
                "\"type\":\"service\"," +
                "\"subType\":\"REST\"," +
                "\"url\":\"http://example.com/order/shoppingcarts\"," +
                "\"methods\":[\"GET\"]," +
                "\"mediaTypes\":[\"application/json\"]," +
                "\"authentication\":\"OAUTH\"" +
                "}";
        final ServiceDependency dependency = new ObjectMapper().readValue(json, ServiceDependency.class);
        final ServiceDependency expected = someRestfulService();
        assertThat(dependency).isEqualTo(expected);
    }

    @Test
    public void shouldIgnoreNullValues() throws JsonProcessingException {
        final ServiceDependency dependency = new ServiceDependency(null, null, null, null, null, null, null, null, null);
        final String json = new ObjectMapper().writeValueAsString(dependency);
        assertThat(json).isEqualTo("{}");
    }

    @Test
    public void shouldBeEqual() {
        assertThat(someRestfulService()).isEqualTo(someRestfulService());
    }

    @Test
    public void shouldHaveSameHashCode() {
        assertThat(someRestfulService().hashCode()).isEqualTo(someRestfulService().hashCode());
    }

    private ServiceDependency someRestfulService() {
        return new ServiceDependency(
                "shoppingcart",
                "order",
                "Imports shoppingcarts",
                "http://example.com/order/shoppingcarts",
                TYPE_SERVICE,
                SUBTYPE_REST,
                singletonList("GET"),
                singletonList("application/json"),
                "OAUTH");
    }
}