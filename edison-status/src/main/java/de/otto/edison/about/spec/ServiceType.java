package de.otto.edison.about.spec;

import de.otto.edison.annotations.Beta;
import net.jcip.annotations.Immutable;

import static de.otto.edison.about.spec.Criticality.NOT_SPECIFIED;

/**
 * Specifies the type of a service, including the business criticality and impact.
 */
@Beta
@Immutable
public class ServiceType {

    public static final String TYPE_REST_SERVICE = "service/rest";
    public static final String TYPE_DATA_IMPORT = "data/import/full";
    public static final String TYPE_DATA_FEED = "data/import/delta";

    /** The kind of service. One of the TYPE_* constants, or other predefined values. */
    public final String type;
    /** Criticality of the specified service for the operation of this service. */
    public final Criticality criticality;
    /** Short description of the impact of outages: what would happen if the system is not operational? */
    public final String disasterImpact;

    /**
     * Creates a ServiceType.
     *
     * @param type The type of the service dependency.
     * @param criticality The criticality of the required service for the operation of this service.
     * @param disasterImpact Short description of the impact of outages: what would happen if the system is not operational?
     *
     * @return ServiceType
     */
    public static ServiceType serviceType(final String type, final Criticality criticality, final String disasterImpact) {
        return new ServiceType(type, criticality, disasterImpact);
    }

    public static ServiceType unspecifiedService() {
        return new ServiceType("not specified", NOT_SPECIFIED, "not specified");
    }

    private ServiceType(final String type, final Criticality criticality, final String disasterImpact) {
        this.type = type;
        this.criticality = criticality;
        this.disasterImpact = disasterImpact;
    }
}
