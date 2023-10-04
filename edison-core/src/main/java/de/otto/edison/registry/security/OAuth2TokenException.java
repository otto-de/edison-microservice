package de.otto.edison.registry.security;

public class OAuth2TokenException extends Exception {

    public OAuth2TokenException() {
        super("Error while fetching access token");
    }

}
