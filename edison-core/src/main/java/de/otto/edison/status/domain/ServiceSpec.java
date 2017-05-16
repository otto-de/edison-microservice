package de.otto.edison.status.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

import static de.otto.edison.status.domain.Criticality.criticality;
import static de.otto.edison.status.domain.Expectations.unspecifiedExpectations;
import static de.otto.edison.status.domain.ServiceType.unspecifiedService;
import static java.util.Collections.singletonList;

/**
 * Information about a dependency to a different service, this application is relying on.
 *
 * @deprecated Replaced by {@link ServiceDependency}. Will be removed in 2.0.0
 */
@Beta
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated
public class ServiceSpec extends ServiceDependency {

    /**
     * Create a specification for a service that is required by this service.
     *
     * @param name A human readable name of the service.
     * @param url A URL that is identifying the required REST API. Generally a prefix of the accessed REST resource.
     * @param type The type of the service dependency.
     * @param expectations Expectations about the required service.
     *
     * @return ServiceSpec for the external service.
     */
    public static ServiceSpec serviceSpec(final String name,
                                          final String url,
                                          final ServiceType type,
                                          final Expectations expectations) {
        return new ServiceSpec(name, url, type, expectations);
    }

    public static ServiceSpec serviceSpec(final String name,
                                          final String url) {
        return new ServiceSpec(name, url, unspecifiedService(), unspecifiedExpectations());
    }

    private ServiceSpec(final String name,
                        final String url,
                        final ServiceType type,
                        final Expectations expectations) {
        super(
                name,
                null,
                null,
                url,
                TYPE_SERVICE,
                SUBTYPE_REST,
                singletonList("GET"),
                null,
                null,
                criticality(type.criticality.level, type.disasterImpact),
                expectations);
    }

}
