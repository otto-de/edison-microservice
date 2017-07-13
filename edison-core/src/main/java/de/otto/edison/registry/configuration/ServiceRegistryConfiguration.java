package de.otto.edison.registry.configuration;

import de.otto.edison.status.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.status.domain.Expectations.expects;

@Configuration
@EnableConfigurationProperties(ServiceRegistryProperties.class)
public class ServiceRegistryConfiguration {

    private final ServiceRegistryProperties serviceRegistryProperties;

    @Autowired
    public ServiceRegistryConfiguration(ServiceRegistryProperties serviceRegistryProperties) {
        this.serviceRegistryProperties = serviceRegistryProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix="edison.serviceregistry", name="enabled", havingValue = "true")
    public ServiceDependency serviceRegistryDependency() {
        return ServiceDependencyBuilder.serviceDependency(serviceRegistryProperties.getServers())
                .withName("Service Registry")
                .withDescription("Registers this service at a service registry")
                .withExpectations(expects(Availability.MEDIUM, Performance.MEDIUM))
                .withCriticality(Criticality.criticality(Level.HIGH, "Service cannot be registered"))
                .build();
    }

}

