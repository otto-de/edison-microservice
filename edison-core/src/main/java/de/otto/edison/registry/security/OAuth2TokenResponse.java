package de.otto.edison.registry.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuth2TokenResponse(@JsonProperty("access_token") String accessToken) {

}
