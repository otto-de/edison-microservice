package de.otto.edison.registry.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OAuth2TokenResponse(@JsonProperty(value = "access_token", required = true) String accessToken) {

}
