package de.otto.edison.example;

import de.otto.edison.example.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class OAuthController {

    private final OAuthService oAuthService;

    @Autowired
    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @RequestMapping(
            value = "/token",
            produces = "application/json",
            method = GET
    )
    public Jwt getTestToken() {
        return oAuthService.getExampleJWTToken();
    }

    @RequestMapping(
            value = "/publicKey",
            produces = "application/json",
            method = GET
    )
    public String getPublicKey() {
        return oAuthService.getPublicKey();
    }

}
