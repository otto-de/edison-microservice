package de.otto.edison.status.domain;

import de.otto.edison.annotations.Beta;

/**
 * Information about a dependency to a different service, this application is relying on.
 */
@Beta
public class ServiceDependency {

    /** A human readable name of the service. */
    public final String name;
    /** A URL that may be used to identify the service. */
    public final String url;
    /** The type of the service dependency. */
    public final ServiceType type;
    /** Expectations about the availability of the service. */
    public final AvailabilityRequirement expectedAvailability;
    /** Expectations about the performance of the service. */
    public final PerformanceRequirement expectedPerformance;

    public static ServiceDependency serviceDependency(final String name,
                                                      final ServiceType type,
                                                      final String url) {
        return new ServiceDependency(name, type, url, AvailabilityRequirement.HIGH, PerformanceRequirement.MEDIUM);
    }

    public static ServiceDependency serviceDependency(final String name,
                                                      final ServiceType type,
                                                      final String url,
                                                      final AvailabilityRequirement expectedAvailability,
                                                      final PerformanceRequirement expectedPerformance) {
        return new ServiceDependency(name, type, url, expectedAvailability, expectedPerformance);
    }

    private ServiceDependency(final String name,
                              final ServiceType type,
                              final String url,
                              final AvailabilityRequirement expectedAvailability,
                              final PerformanceRequirement expectedPerformance) {
        this.name = name;
        this.type = type;
        this.url = url;
        this.expectedAvailability = expectedAvailability;
        this.expectedPerformance = expectedPerformance;
    }


}
