package de.otto.edison.status.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static de.otto.edison.status.domain.Criticality.criticality;
import static de.otto.edison.status.domain.Expectations.unspecifiedExpectations;
import static de.otto.edison.status.domain.ServiceDependency.SUBTYPE_REST;
import static de.otto.edison.status.domain.ServiceDependency.TYPE_SERVICE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class ServiceDependencyTest {

    @Test
    public void shouldTransformToJson() throws JsonProcessingException {
        final ServiceDependency dependency = someRestfulService();
        final String json = new ObjectMapper().writeValueAsString(dependency);
        assertThat(json).isEqualTo("{" +
                "\"name\":\"shoppingcart\"," +
                "\"description\":" +
                "\"Imports shoppingcarts\"," +
                "\"type\":\"service\"," +
                "\"subtype\":\"REST\"," +
                "\"criticality\":{\"level\":\"HIGH\",\"disasterImpact\":\"really bad\"}," +
                "\"expectations\":{\"availability\":\"NOT_SPECIFIED\",\"performance\":\"NOT_SPECIFIED\"}," +
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
                "\"description\":" +
                "\"Imports shoppingcarts\"," +
                "\"type\":\"service\"," +
                "\"subtype\":\"REST\"," +
                "\"criticality\":{\"level\":\"HIGH\",\"disasterImpact\":\"really bad\"}," +
                "\"expectations\":{\"availability\":\"NOT_SPECIFIED\",\"performance\":\"NOT_SPECIFIED\"}," +
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
        final ServiceDependency dependency = new ServiceDependency(null, null, "", "", "", null, null, null, null, null);
        final String json = new ObjectMapper().writeValueAsString(dependency);
        assertThat(json).isEqualTo("{\"name\":\"\",\"description\":\"\",\"type\":\"\",\"subtype\":\"\",\"criticality\":{\"level\":\"NOT_SPECIFIED\",\"disasterImpact\":\"Not Specified\"},\"expectations\":{\"availability\":\"NOT_SPECIFIED\",\"performance\":\"NOT_SPECIFIED\"},\"url\":\"\",\"methods\":[],\"mediaTypes\":[],\"authentication\":\"\"}");
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
                "Imports shoppingcarts",
                "http://example.com/order/shoppingcarts",
                TYPE_SERVICE,
                SUBTYPE_REST,
                singletonList("GET"),
                singletonList("application/json"),
                "OAUTH",
                criticality(Level.HIGH, "really bad"),
                unspecifiedExpectations());
    }
}