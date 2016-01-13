package de.otto.edison.status.domain;

import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

import static de.otto.edison.status.domain.Expectations.unspecifiedExpectations;

/**
 * Information about a dependency to a different service, this application is relying on.
 */
@Beta
@Immutable
public class ServiceSpec {

    /** The appId of the service. See {@link de.otto.edison.status.domain.ApplicationInfo#appId}. */
    public final String appId;
    /** A human readable name of the service. */
    public final String name;
    /** A URL that is identifying the required REST API. Generally a prefix of the accessed REST resource. */
    public final String url;
    /** The type of the service dependency. */
    public final ServiceType type;
    /** Expectations about the required service. */
    public final Expectations expectations;

    /**
     * Create a specification for a service that is required by this service.
     *
     * @param appId The appId of the service. See {@link de.otto.edison.status.domain.ApplicationInfo#appId}.
     * @param name A human readable name of the service.
     * @param url A URL that is identifying the required REST API. Generally a prefix of the accessed REST resource.
     * @param type The type of the service dependency.
     * @param expectations Expectations about the required service.
     *
     * @return ServiceSpec for the external service.
     */
    public static ServiceSpec serviceSpec(final String appId,
                                          final String name,
                                          final String url,
                                          final ServiceType type,
                                          final Expectations expectations) {
        return new ServiceSpec(appId, name, url, type, expectations);
    }

    public static ServiceSpec serviceSpec(final String appId,
                                          final String name,
                                          final String url) {
        return new ServiceSpec(appId, name, url, ServiceType.unspecifiedService(), unspecifiedExpectations());
    }

    private ServiceSpec(final String appId,
                        final String name,
                        final String url,
                        final ServiceType type,
                        final Expectations expectations) {
        this.appId = appId;
        this.name = name;
        this.type = type;
        this.url = url;
        this.expectations = expectations;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceSpec that = (ServiceSpec) o;

        if (appId != null ? !appId.equals(that.appId) : that.appId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return !(expectations != null ? !expectations.equals(that.expectations) : that.expectations != null);

    }

    @Override
    public int hashCode() {
        int result = appId != null ? appId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (expectations != null ? expectations.hashCode() : 0);
        return result;
    }
}
