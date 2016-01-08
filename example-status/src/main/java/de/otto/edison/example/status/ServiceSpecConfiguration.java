package de.otto.edison.example.status;

import de.otto.edison.about.spec.AvailabilityRequirement;
import de.otto.edison.about.spec.PerformanceRequirement;
import de.otto.edison.about.spec.ServiceSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.about.spec.ServiceSpec.serviceSpec;
import static de.otto.edison.about.spec.ServiceType.DATA_PROVISIONING;
import static de.otto.edison.about.spec.ServiceType.SERVICE;

/**
 * An example for a configuration of some dependencies to other services.
 *
 * This information is gathered by Edison to provide information about such dependencies.
 * The data is exposed by the /internal/about API for external usage.
 *
 * Created by guido on 07.01.16.
 */
@Configuration
public class ServiceSpecConfiguration {

    @Bean
    ServiceSpec fooClient() {
        return serviceSpec("Foo Service", SERVICE, "http://example.org/api/foo", AvailabilityRequirement.HIGH, PerformanceRequirement.HIGH);
    }

    @Bean
    ServiceSpec barClient() {
        return serviceSpec("Bar Service", DATA_PROVISIONING, "http://example.org/api/bar", AvailabilityRequirement.MEDIUM, PerformanceRequirement.LOW);
    }
}
