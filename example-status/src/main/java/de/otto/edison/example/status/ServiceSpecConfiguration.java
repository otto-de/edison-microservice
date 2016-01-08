package de.otto.edison.example.status;

import de.otto.edison.about.spec.ServiceSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.about.spec.Criticality.FUNCTIONAL_CRITICAL;
import static de.otto.edison.about.spec.Expectations.lowExpectations;
import static de.otto.edison.about.spec.ServiceSpec.serviceSpec;
import static de.otto.edison.about.spec.ServiceType.TYPE_DATA_FEED;
import static de.otto.edison.about.spec.ServiceType.serviceType;

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
        return serviceSpec("/local/example/foo", "Foo Service", "http://example.org/api/foo");
    }

    @Bean
    ServiceSpec barClient() {
        return serviceSpec(
                "/local/example/bar", "Bar Service", "http://example.org/api/bar",
                serviceType(TYPE_DATA_FEED, FUNCTIONAL_CRITICAL, "Data will become inconsistent"),
                lowExpectations()
        );
    }
}
