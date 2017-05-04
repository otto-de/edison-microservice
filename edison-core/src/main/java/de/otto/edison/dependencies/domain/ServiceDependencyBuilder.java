package de.otto.edison.dependencies.domain;

import java.util.List;

public class ServiceDependencyBuilder {
    private String name;
    private String group;
    private String description;
    private String url;
    private String type;
    private String subtype;
    private List<String> methods;
    private List<String> mediaTypes;
    private String authentication;

    public static ServiceDependencyBuilder copyOf(final ServiceDependency other) {
        return new ServiceDependencyBuilder()
                .withName(other.name)
                .withGroup(other.group)
                .withDescription(other.description)
                .withUrl(other.url)
                .withType(other.type)
                .withSubtype(other.subtype)
                .withMethods(other.methods)
                .withMediaTypes(other.mediaTypes)
                .withAuthentication(other.authentication);
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
                .withSubtype(ServiceDependency.SUBTYPE_REST);
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
     */
    public ServiceDependencyBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * @param group The service group like, for example, the vertical aka SCS the service is belonging to.
     */
    public ServiceDependencyBuilder withGroup(final String group) {
        this.group = group;
        return this;
    }

    /**
     * @param description A human readable description of the dependency.
     */
    public ServiceDependencyBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    /**
     * @param url A URL that is identifying the dependency. Generally an URI template of the accessed REST
     * resource or the URI of the database.
     */
    private ServiceDependencyBuilder withUrl(final String url) {
        this.url = url;
        return this;
    }

    /**
     * @param type The type of the dependency: db, queue, service, ...
     */
    public ServiceDependencyBuilder withType(final String type) {
        this.type = type;
        return this;
    }

    /**
     * @param subtype The sub-type of the dependency: Cassandra, MongoDB, Kafka, REST, ...
     */
    public ServiceDependencyBuilder withSubtype(final String subtype) {
        this.subtype = subtype;
        return this;
    }

    /**
     * @param methods HTTP Methods used to access the service (GET, PUT, POST, DELETE, HEAD, ...)
     */
    public ServiceDependencyBuilder withMethods(final List<String> methods) {
        this.methods = methods;
        return this;
    }

    /**
     * @param mediaTypes The MediaType used to access a REST service
     */
    public ServiceDependencyBuilder withMediaTypes(final List<String> mediaTypes) {
        this.mediaTypes = mediaTypes;
        return this;
    }

    /**
     * @param authentication Authentication scheme used to access the remote service (HMAC, OAUTH, ...)
     */
    public ServiceDependencyBuilder withAuthentication(final String authentication) {
        this.authentication = authentication;
        return this;
    }

    /**
     * Builds a ServiceDependency instance.
     *
     * @return service dependency
     */
    public ServiceDependency build() {
        return new ServiceDependency(name, group, description, url, type, subtype, methods, mediaTypes, authentication);
    }
}