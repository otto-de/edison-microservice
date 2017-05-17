package de.otto.edison.status.domain;

import de.otto.edison.annotations.Beta;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * A builder used to build {@link ServiceDependency service dependencies}
 *
 * @since 1.1.0
 */
@Beta
public class ServiceDependencyBuilder {
    private String name;
    private String description;
    private String url;
    private String type;
    private String subtype;
    private List<String> methods;
    private List<String> mediaTypes;
    private String authentication;
    private Criticality criticality;
    private Expectations expectations;

    /**
     * Returns a builder instance that is initialized using a prototype ServiceDependency.
     * <p>
     *     All values of the prototype are copied.
     * </p>
     * @param prototype the prototype dependency
     * @return ServiceDependencyBuilder
     */
    public static ServiceDependencyBuilder copyOf(final ServiceDependency prototype) {
        return new ServiceDependencyBuilder()
                .withName(prototype.getName())
                .withDescription(prototype.getDescription())
                .withUrl(prototype.getUrl())
                .withType(prototype.getType())
                .withSubtype(prototype.getSubtype())
                .withMethods(prototype.getMethods())
                .withMediaTypes(prototype.getMediaTypes())
                .withAuthentication(prototype.getAuthentication())
                .withCriticality(prototype.getCriticality())
                .withExpectations(prototype.getExpectations());
    }

    /**
     * Creates a ServiceDependencyBuilder with type="service" and subtype="REST".
     *
     * @param url the url or uri-template of the accessed REST service.
     * @return ServiceDependencyBuilder
     */
    public static ServiceDependencyBuilder restServiceDependency(final String url) {
        return new ServiceDependencyBuilder()
                .withUrl(url)
                .withType(ServiceDependency.TYPE_SERVICE)
                .withSubtype(ServiceDependency.SUBTYPE_REST)
                .withMethods(singletonList("GET"))
                .withMediaTypes(singletonList("application/json"));
    }

    /**
     * Creates a generic ServiceDependencyBuilder with type="service" and subtype="OTHER".
     *
     * @param url the url or uri-template of the accessed service.
     * @return ServiceDependencyBuilder
     */
    public static ServiceDependencyBuilder serviceDependency(final String url) {
        return new ServiceDependencyBuilder()
                .withUrl(url)
                .withType(ServiceDependency.TYPE_SERVICE)
                .withSubtype(ServiceDependency.SUBTYPE_OTHER);
    }

    /**
     * @param name The name of the dependent service.
     * @return this
     */
    public ServiceDependencyBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * @param description A human readable description of the dependency.
     * @return this
     */
    public ServiceDependencyBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    /**
     * @param url A URL that is identifying the dependency. Generally an URI template of the accessed REST
     * resource or the URI of the database.
     * @return this
     */
    private ServiceDependencyBuilder withUrl(final String url) {
        this.url = url;
        return this;
    }

    /**
     * @param type The type of the dependency: db, queue, service, ...
     * @return this
     */
    public ServiceDependencyBuilder withType(final String type) {
        this.type = type;
        return this;
    }

    /**
     * @param subtype The sub-type of the dependency: Cassandra, MongoDB, Kafka, REST, ...
     * @return this
     */
    public ServiceDependencyBuilder withSubtype(final String subtype) {
        this.subtype = subtype;
        return this;
    }

    /**
     * @param methods HTTP Methods used to access the service (GET, PUT, POST, DELETE, HEAD, ...)
     * @return this
     */
    public ServiceDependencyBuilder withMethods(final List<String> methods) {
        this.methods = methods;
        return this;
    }

    /**
     * @param methods HTTP Methods used to access the service (GET, PUT, POST, DELETE, HEAD, ...)
     * @return this
     */
    public ServiceDependencyBuilder withMethods(final String... methods) {
        this.methods = asList(methods);
        return this;
    }

    /**
     * @param mediaTypes The MediaType used to access a REST service
     * @return this
     */
    public ServiceDependencyBuilder withMediaTypes(final List<String> mediaTypes) {
        this.mediaTypes = mediaTypes;
        return this;
    }

    /**
     * @param mediaTypes The MediaType used to access a REST service
     * @return this
     */
    public ServiceDependencyBuilder withMediaTypes(final String... mediaTypes) {
        this.mediaTypes = asList(mediaTypes);
        return this;
    }

    /**
     * @param authentication Authentication scheme used to access the remote service (HMAC, OAUTH, ...)
     * @return this
     */
    public ServiceDependencyBuilder withAuthentication(final String authentication) {
        this.authentication = authentication;
        return this;
    }

    /**
     * @param criticality The criticality of the dependency
     * @return this
     */
    public ServiceDependencyBuilder withCriticality(final Criticality criticality) {
        this.criticality = criticality;
        return this;
    }

    /**
     * @param expectations The expectations of of the dependency
     * @return this
     */
    public ServiceDependencyBuilder withExpectations(final Expectations expectations) {
        this.expectations = expectations;
        return this;
    }

    /**
     * Builds a ServiceDependency instance.
     *
     * @return service dependency
     */
    public ServiceDependency build() {
        return new ServiceDependency(name, description, url, type, subtype, methods, mediaTypes, authentication, criticality, expectations);
    }
}