package de.otto.edison.registry.security;

import de.otto.edison.registry.configuration.ServiceRegistrySecurityOAuthProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(ServiceRegistrySecurityOAuthProperties.class)
public class OAuth2TokenProviderFactory {

    private final ServiceRegistrySecurityOAuthProperties properties;

    @Autowired
    public OAuth2TokenProviderFactory(final ServiceRegistrySecurityOAuthProperties properties) {
        this.properties = properties;
    }

    public OAuth2TokenProvider create() {
        return new OAuth2TokenProvider(properties.clientId(), properties.clientSecret(), properties.tokenEndpoint(), properties.timeoutSeconds());
    }

    public boolean isEnabled() {
        return properties.enabled();
    }
}
