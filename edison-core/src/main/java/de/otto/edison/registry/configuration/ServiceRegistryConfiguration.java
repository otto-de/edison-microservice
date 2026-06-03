package de.otto.edison.registry.configuration;

import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.registry.client.AsyncHttpRegistryClient;
import de.otto.edison.registry.security.OAuth2TokenProviderFactory;
import de.otto.edison.status.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static de.otto.edison.status.domain.Expectations.expects;

@AutoConfiguration
@EnableConfigurationProperties({ServiceRegistryProperties.class, ServiceRegistrySecurityOAuthProperties.class})
public class ServiceRegistryConfiguration {

    private final ServiceRegistryProperties serviceRegistryProperties;

    @Autowired
    public ServiceRegistryConfiguration(ServiceRegistryProperties serviceRegistryProperties) {
        this.serviceRegistryProperties = serviceRegistryProperties;
    }

    @Bean
    public OAuth2TokenProviderFactory oAuth2TokenProviderFactory(final ServiceRegistrySecurityOAuthProperties properties) {
        return new OAuth2TokenProviderFactory(properties);
    }

    @Bean
    public AsyncHttpRegistryClient asyncHttpRegistryClient(final ApplicationInfo applicationInfo,
                                                           final EdisonApplicationProperties edisonApplicationProperties,
                                                           final OAuth2TokenProviderFactory oAuth2TokenProviderFactory) {
        return new AsyncHttpRegistryClient(applicationInfo, serviceRegistryProperties, edisonApplicationProperties, oAuth2TokenProviderFactory);
    }

    @Bean
    @ConditionalOnProperty(prefix = "edison.serviceregistry", name = "enabled", havingValue = "true")
    public ServiceDependency serviceRegistryDependency() {
        return ServiceDependencyBuilder.serviceDependency(serviceRegistryProperties.getServers())
                .withName("Service Registry")
                .withDescription("Registers this service at a service registry")
                .withExpectations(expects(Availability.MEDIUM, Performance.MEDIUM))
                .withCriticality(Criticality.criticality(Level.HIGH, "Service cannot be registered"))
                .build();
    }

}
