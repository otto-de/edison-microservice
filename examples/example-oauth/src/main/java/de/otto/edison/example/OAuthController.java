package de.otto.edison.example;

import de.otto.edison.example.oauth.OAuthService;
import de.otto.edison.oauth.OAuthPublicKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class OAuthController {

    private final OAuthService oAuthService;

    @Autowired
    public OAuthController(final OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @RequestMapping(
            value = "/token",
            produces = "application/json",
            method = GET
    )
    @ResponseBody
    public Jwt getTestToken() {
        return oAuthService.getExampleJWTToken();
    }

    @RequestMapping(
            value = "/publicKey",
            produces = "application/json",
            method = GET
    )
    @ResponseBody
    public List<OAuthPublicKey> getPublicKey() {
        return Collections.singletonList(oAuthService.getPublicKey());
    }

}
