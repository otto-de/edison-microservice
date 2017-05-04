package de.otto.edison.dependencies.domain;

import org.junit.Test;

import static de.otto.edison.dependencies.domain.ServiceDependencyBuilder.copyOf;
import static de.otto.edison.dependencies.domain.ServiceDependencyBuilder.restServiceDependency;
import static de.otto.edison.dependencies.domain.ServiceDependencyBuilder.serviceDependency;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class ServiceDependencyBuilderTest {

    @Test
    public void shouldBuildServiceDependency() {
        final ServiceDependency dependency = serviceDependency("http://example.com")
                .withName("name")
                .withDescription("description")
                .withGroup("search")
                .withMediaTypes(singletonList("application/json"))
                .withAuthentication("HMAC")
                .withMethods(asList("GET", "HEAD"))
                .build();
        assertThat(dependency.name).isEqualTo("name");
        assertThat(dependency.description).isEqualTo("description");
        assertThat(dependency.group).isEqualTo("search");
        assertThat(dependency.authentication).isEqualTo("HMAC");
        assertThat(dependency.mediaTypes).contains("application/json");
        assertThat(dependency.methods).contains("GET", "HEAD");
        assertThat(dependency.type).isEqualTo("service");
        assertThat(dependency.subtype).isEqualTo("OTHER");
        assertThat(dependency.url).isEqualTo("http://example.com");
    }

    @Test
    public void shouldBuildRestServiceDependency() {
        final ServiceDependency dependency = restServiceDependency("http://example.com")
                .withName("name")
                .withDescription("description")
                .withGroup("search")
                .withMediaTypes(singletonList("application/json"))
                .withAuthentication("HMAC")
                .withMethods(asList("GET", "HEAD"))
                .build();
        assertThat(dependency.name).isEqualTo("name");
        assertThat(dependency.description).isEqualTo("description");
        assertThat(dependency.group).isEqualTo("search");
        assertThat(dependency.authentication).isEqualTo("HMAC");
        assertThat(dependency.mediaTypes).contains("application/json");
        assertThat(dependency.methods).contains("GET", "HEAD");
        assertThat(dependency.type).isEqualTo("service");
        assertThat(dependency.subtype).isEqualTo("REST");
        assertThat(dependency.url).isEqualTo("http://example.com");
    }

    @Test
    public void shouldCopyServiceDependency() {
        final ServiceDependency dependency = serviceDependency("http://example.com")
                .withName("name")
                .withDescription("description")
                .withGroup("search")
                .withMediaTypes(singletonList("application/json"))
                .withAuthentication("HMAC")
                .withMethods(asList("GET", "HEAD"))
                .withType("some type")
                .withSubtype("some subtype")
                .build();
        assertThat(dependency).isEqualTo(copyOf(dependency).build());
        assertThat(dependency.hashCode()).isEqualTo(copyOf(dependency).build().hashCode());
    }

}