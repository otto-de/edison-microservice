package de.otto.edison.example.status;

import de.otto.edison.status.domain.AvailabilityRequirement;
import de.otto.edison.status.domain.ServiceDependency;
import de.otto.edison.status.domain.PerformanceRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.status.domain.ServiceDependency.serviceDependency;
import static de.otto.edison.status.domain.ServiceType.*;

/**
 * An example for a configuration of some dependencies to other services.
 *
 * This information is gathered by Edison to provide information about such dependencies.
 * The data is exposed by the /internal/about API for external usage.
 *
 * Created by guido on 07.01.16.
 */
@Configuration
public class ClientDescriptionConfiguration {

    @Bean
    ServiceDependency fooClient() {
        return serviceDependency("Foo Service", SERVICE, "http://example.org/api/foo", AvailabilityRequirement.HIGH, PerformanceRequirement.HIGH);
    }

    @Bean
    ServiceDependency barClient() {
        return serviceDependency("Bar Service", DATA_PROVISIONING, "http://example.org/api/bar", AvailabilityRequirement.MEDIUM, PerformanceRequirement.LOW);
    }
}
