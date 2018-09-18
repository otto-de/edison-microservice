package de.otto.edison.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.time.ZonedDateTime;

@Component
public class OAuthTestHelper {

    @Autowired
    AuthorizationServerTokenServices tokenservice;

    @Autowired
    KeyPair keyPair;

    @Value("${edison.oauth.jwt.audience}")
    String aud;

    public String getBearerToken(final String scope) {
        final ZonedDateTime soon = ZonedDateTime.now().plusDays(365);
        final String jwtToken = "{\n" +
                "  \"aud\": [\n" +
                "    \"" + aud + "\"\n" +
                "  ],\n" +
                "  \"exp\": " + soon.toEpochSecond() + ",\n" +
                "  \"user_name\": \"3d44bbc24614e28edd094bc54ef0497809717af5\",\n" +
                "  \"jti\": \"3cee521d-96a7-4d82-b726-7e02355f3a55\",\n" +
                "  \"client_id\": \"fe0661e5a99e4d43bd3496cc6c58025f\",\n" +
                "  \"scope\": [\n" +
                "    \"" + scope + "\"\n" +
                "  ]\n" +
                "}";
        final RsaSigner rsaSigner = new RsaSigner((RSAPrivateKey) keyPair.getPrivate());
        final Jwt encode = JwtHelper.encode(jwtToken, rsaSigner);


        return "Bearer " + encode.getEncoded();
    }
}
