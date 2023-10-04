package de.otto.edison.registry.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "edison.serviceregistry.security.oauth2")
public record ServiceRegistrySecurityOAuthProperties(@DefaultValue("false") boolean enabled,
                                                     String tokenEndpoint,
                                                     String clientId,
                                                     String clientSecret,
                                                     @DefaultValue("10") int timeoutSeconds) {

}
