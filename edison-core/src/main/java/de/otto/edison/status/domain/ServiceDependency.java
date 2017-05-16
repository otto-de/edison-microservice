package de.otto.edison.status.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Collections.emptyList;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

/**
 * A dependency to a RESTful or other kind of service.
 *
 * @since 1.1.0
 */
@Beta
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class ServiceDependency extends ExternalDependency {
    public static final String TYPE_SERVICE = "service";

    public static final String SUBTYPE_REST = "REST";
    public static final String SUBTYPE_OTHER = "OTHER";

    public static final String AUTH_BASIC = "BASIC";
    public static final String AUTH_DIGEST = "DIGEST";
    public static final String AUTH_HMAC = "HMAC";
    public static final String AUTH_OAUTH = "OAUTH";
    public static final String AUTH_NONE = "NONE";

    private final String url;

    private final List<String> methods;
    private final List<String> mediaTypes;
    private final String authentication;

    ServiceDependency() {
        this(null, null, null, "", "", "", null, null, null, null, null);
    }

    /**
     * Creates a ServiceDependency.
     * <p>
     *     In most cases, using {@link ServiceDependencyBuilder} is more appropriate to create
     *     ServiceDependency instances.
     * </p>
     *
     * @param name The name of the dependent service or datasource
     * @param group The service group like, for example, the vertical aka SCS the service is belonging to.
     * @param description A human readable description of the dependency.
     * @param type The type of the dependency: db, queue, service, ...
     * @param subtype The sub-type of the dependency: Cassandra, MongoDB, Kafka, REST, ...
     * @param criticality Describes the criticality of the dependency for this service.
     * @param expectations Expectations of this service to the external dependency with respect to availability and performance.
     * @param url A URL that is identifying the dependency. Generally an URI template of the accessed REST resource.
     * @param methods HTTP Methods used to access the service (GET, PUT, POST, DELETE, HEAD, ...)
     * @param mediaTypes The MediaType used to access a REST service
     * @param authentication Authentication scheme used to access the remote service (HMAC, OAUTH, ...)
     */
    public ServiceDependency(final String name,
                             final String group,
                             final String description,
                             final String url,
                             final String type,
                             final String subtype,
                             final List<String> methods,
                             final List<String> mediaTypes,
                             final String authentication,
                             final Criticality criticality,
                             final Expectations expectations) {
        super(name, group, description, type, subtype, criticality, expectations);
        this.url = requireNonNull(url, "Parameter 'url' must not be null");
        this.methods = methods != null ? methods : emptyList();
        this.mediaTypes = mediaTypes != null ? mediaTypes : emptyList();
        this.authentication = Objects.toString(authentication, "");
    }

    /**
     * @return A URL that is identifying the dependency. Generally an URI template of the accessed REST resource.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return HTTP Methods used to access the service (GET, PUT, POST, DELETE, HEAD, ...)
     */
    public List<String> getMethods() {
        return methods;
    }

    /**
     * @return The MediaType used to access a REST service
     */
    public List<String> getMediaTypes() {
        return mediaTypes;
    }

    /**
     * @return Authentication scheme used to access the remote service (HMAC, OAUTH, ...)
     */
    public String getAuthentication() {
        return authentication;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceDependency)) return false;
        if (!super.equals(o)) return false;
        ServiceDependency that = (ServiceDependency) o;
        return Objects.equals(getUrl(), that.getUrl()) &&
                Objects.equals(getMethods(), that.getMethods()) &&
                Objects.equals(getMediaTypes(), that.getMediaTypes()) &&
                Objects.equals(getAuthentication(), that.getAuthentication());
    }

    @Override
    public int hashCode() {
        return hash(super.hashCode(), getUrl(), getMethods(), getMediaTypes(), getAuthentication());
    }

    @Override
    public String toString() {
        return "ServiceDependency{" +
                "url='" + getUrl() + '\'' +
                ", methods=" + getMethods() +
                ", mediaTypes=" + getMediaTypes() +
                ", authentication='" + getAuthentication() + '\'' +
                "} " + super.toString();
    }

}
