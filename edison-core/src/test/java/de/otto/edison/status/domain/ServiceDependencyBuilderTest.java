package de.otto.edison.status.domain;

import org.junit.Test;

import static de.otto.edison.status.domain.ServiceDependency.AUTH_HMAC;
import static de.otto.edison.status.domain.ServiceDependency.AUTH_OAUTH;
import static de.otto.edison.status.domain.ServiceDependencyBuilder.copyOf;
import static de.otto.edison.status.domain.ServiceDependencyBuilder.restServiceDependency;
import static de.otto.edison.status.domain.ServiceDependencyBuilder.serviceDependency;
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
                .withMethods(asList("GET", "HEAD"))
                .withAuthentication(AUTH_HMAC)
                .build();
        assertThat(dependency.getName()).isEqualTo("name");
        assertThat(dependency.getDescription()).isEqualTo("description");
        assertThat(dependency.getGroup()).isEqualTo("search");
        assertThat(dependency.getAuthentication()).isEqualTo("HMAC");
        assertThat(dependency.getMediaTypes()).contains("application/json");
        assertThat(dependency.getMethods()).contains("GET", "HEAD");
        assertThat(dependency.getType()).isEqualTo("service");
        assertThat(dependency.getSubtype()).isEqualTo("OTHER");
        assertThat(dependency.getUrl()).isEqualTo("http://example.com");
    }

    @Test
    public void shouldBuildRestServiceDependency() {
        final ServiceDependency dependency = restServiceDependency("http://example.com")
                .withName("name")
                .withDescription("description")
                .withGroup("search")
                .withMediaTypes(singletonList("application/json"))
                .withAuthentication(AUTH_OAUTH)
                .withMethods(asList("GET", "HEAD"))
                .build();
        assertThat(dependency.getName()).isEqualTo("name");
        assertThat(dependency.getDescription()).isEqualTo("description");
        assertThat(dependency.getGroup()).isEqualTo("search");
        assertThat(dependency.getAuthentication()).isEqualTo("OAUTH");
        assertThat(dependency.getMediaTypes()).contains("application/json");
        assertThat(dependency.getMethods()).contains("GET", "HEAD");
        assertThat(dependency.getType()).isEqualTo("service");
        assertThat(dependency.getSubtype()).isEqualTo("REST");
        assertThat(dependency.getUrl()).isEqualTo("http://example.com");
    }

    @Test
    public void shouldCopyServiceDependency() {
        final ServiceDependency dependency = serviceDependency("http://example.com")
                .withName("name")
                .withDescription("description")
                .withGroup("search")
                .withMediaTypes(singletonList("application/json"))
                .withAuthentication(AUTH_HMAC)
                .withMethods(asList("GET", "HEAD"))
                .withType("some type")
                .withSubtype("some subtype")
                .build();
        assertThat(dependency).isEqualTo(copyOf(dependency).build());
        assertThat(dependency.hashCode()).isEqualTo(copyOf(dependency).build().hashCode());
    }

}