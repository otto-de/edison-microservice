package de.otto.edison.example.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.otto.edison.mongo.configuration.MongoProperties;
import de.otto.edison.status.domain.Criticality;
import de.otto.edison.status.domain.DatasourceDependency;
import de.otto.edison.status.domain.ServiceDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static de.otto.edison.status.domain.Criticality.FUNCTIONAL_CRITICAL;
import static de.otto.edison.status.domain.Criticality.criticality;
import static de.otto.edison.status.domain.DatasourceDependencyBuilder.mongoDependency;
import static de.otto.edison.status.domain.Expectations.highExpectations;
import static de.otto.edison.status.domain.Level.HIGH;
import static de.otto.edison.status.domain.Level.LOW;
import static de.otto.edison.status.domain.ServiceDependency.AUTH_HMAC;
import static de.otto.edison.status.domain.ServiceDependencyBuilder.restServiceDependency;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;

@Configuration
public class ExampleMongoConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(lookup().lookupClass());

    @Bean
    public MongoClient mongoClient(final MongoProperties mongoProperties) throws IOException {
        return MongoClients.create();
    }

    @Bean
    public Criticality serviceCriticality() {
        return criticality(LOW, "This is only a test, so the disaster impact should be quite low.");
    }

    @Bean
    public DatasourceDependency togglzMongoDependency(final MongoProperties mongoProperties) {
        return mongoDependency(mongoProperties.toDatasources())
                .withName("Togglz DB")
                .withDescription("Database used to store the state of the toggles")
                .withCriticality(criticality(HIGH, "Unable to use Togglz"))
                .withExpectations(highExpectations())
                .build();
    }

    @Bean
    public ServiceDependency ottoDependency() {
        return restServiceDependency("http://api.otto.de")
                .withAuthentication(AUTH_HMAC)
                .withMethods(asList("GET", "HEAD"))
                .withMediaTypes(asList("application/json", "application/hal+json"))
                .withName("OTTO API")
                .withDescription("Just an example to show how to configure a dependency to a REST service.")
                .withExpectations(highExpectations())
                .withCriticality(FUNCTIONAL_CRITICAL)
                .build();
    }

}
