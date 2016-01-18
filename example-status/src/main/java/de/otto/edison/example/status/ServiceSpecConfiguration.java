package de.otto.edison.example.status;

import de.otto.edison.status.domain.ServiceSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.status.domain.Criticality.FUNCTIONAL_CRITICAL;
import static de.otto.edison.status.domain.Expectations.lowExpectations;
import static de.otto.edison.status.domain.ServiceSpec.serviceSpec;
import static de.otto.edison.status.domain.ServiceType.TYPE_DATA_FEED;
import static de.otto.edison.status.domain.ServiceType.serviceType;

/**
 * An example for a configuration of some dependencies to other services.
 *
 * This information is gathered by Edison to provide information about such dependencies.
 * The data is exposed by the /internal/status API for external usage.
 *
 * Created by guido on 07.01.16.
 */
@Configuration
public class ServiceSpecConfiguration {

    @Bean
    ServiceSpec fooClient() {
        return serviceSpec("Foo Service", "http://example.org/api/foo");
    }

    @Bean
    ServiceSpec barClient() {
        return serviceSpec(
                "Bar Service", "http://example.org/api/bar",
                serviceType(TYPE_DATA_FEED, FUNCTIONAL_CRITICAL, "Data will become inconsistent"),
                lowExpectations()
        );
    }
}
