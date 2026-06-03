package de.otto.edison.registry.security;

import de.otto.edison.registry.configuration.ServiceRegistrySecurityOAuthProperties;

public class OAuth2TokenProviderFactory {

    private final ServiceRegistrySecurityOAuthProperties properties;

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
