package de.otto.edison.dependencies.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

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

    /**
     * A URL that is identifying the dependency. Generally an URI template of the accessed REST resource.
     */
    public final String url;

    /**
     * HTTP Methods used to access the service (GET, PUT, POST, DELETE, HEAD, ...)
     */
    public final List<String> methods;
    /**
     * The MediaType used to access a REST service
     */
    public final List<String> mediaTypes;
    /**
     * Authentication scheme used to access the remote service (HMAC, OAUTH, ...)
     */
    public final String authentication;

    ServiceDependency() {
        this(null, null, null, null, null, null, null, null, null);
    }

    public ServiceDependency(final String name,
                             final String group,
                             final String description,
                             final String url,
                             final String type,
                             final String subtype,
                             final List<String> methods,
                             final List<String> mediaTypes,
                             final String authentication) {
        super(name, group, description, type, subtype);
        this.url = url;
        this.methods = methods;
        this.mediaTypes = mediaTypes;
        this.authentication = authentication;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceDependency)) return false;
        if (!super.equals(o)) return false;
        ServiceDependency that = (ServiceDependency) o;
        return Objects.equals(url, that.url) &&
                Objects.equals(methods, that.methods) &&
                Objects.equals(mediaTypes, that.mediaTypes) &&
                Objects.equals(authentication, that.authentication);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, methods, mediaTypes, authentication);
    }

    @Override
    public String toString() {
        return "ServiceDependency{" +
                "url='" + url + '\'' +
                ", methods=" + methods +
                ", mediaTypes=" + mediaTypes +
                ", authentication='" + authentication + '\'' +
                "} " + super.toString();
    }
}
