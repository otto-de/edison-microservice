package de.otto.edison.example.status;

import de.otto.edison.status.domain.ExternalDependency;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.status.domain.Criticality.mediumCriticality;
import static de.otto.edison.status.domain.Expectations.lowExpectations;
import static de.otto.edison.status.domain.ServiceDependencyBuilder.restServiceDependency;

/**
 * An example for a configuration of some dependencies to other services.
 *
 * This information is gathered by Edison to provide information about such dependencies.
 * The data is exposed by the /internal/status API for external usage.
 *
 * Created by guido on 07.01.16.
 */
@Configuration
public class DependenciesConfiguration {

    @Bean
    ExternalDependency fooClient() {
        return restServiceDependency("http://example.org/api/foo").withName("Foo Service").build();
    }

    @Bean
    ExternalDependency barClient() {
        return restServiceDependency("http://example.org/api/bar")
                .withName("Bar Service")
                .withCriticality(mediumCriticality("Data will become inconsistent"))
                .withExpectations(lowExpectations())
                .build();
    }
}
