package de.otto.edison.example.configuration;

import de.otto.edison.oauth.KeyExchangeJwtAccessTokenConverter;
import de.otto.edison.oauth.OAuthPublicKey;
import de.otto.edison.oauth.OAuthPublicKeyRepository;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.List;

import static java.time.ZonedDateTime.now;

@Configuration
public class OAuthPublicKeyTestConfiguration {

    @Bean
    @Profile("test")
    public AsyncHttpClient asyncHttpClient() {
        final AsyncHttpClientConfig httpClientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setRequestTimeout(1000)
                .setReadTimeout(1000)
                .build();
        return new DefaultAsyncHttpClient(httpClientConfig);
    }

    @Bean
    @Profile("test")
    public KeyPair testKeyPair() throws NoSuchAlgorithmException {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        return keyGen.generateKeyPair();
    }

    @Bean
    @Profile("test")
    public AuthorizationServerTokenServices testAuthorizationServerTokenServices(final KeyExchangeJwtAccessTokenConverter keyExchangeJwtAccessTokenConverter,
                                                                                 final KeyPair keyPair) {
        final DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        keyExchangeJwtAccessTokenConverter.setKeyPair(keyPair);
        defaultTokenServices.setTokenStore(new JwtTokenStore(keyExchangeJwtAccessTokenConverter));
        return defaultTokenServices;
    }

    @Bean
    @Profile("test")
    @Primary
    public OAuthPublicKeyRepository inMemoryTestRepository(final KeyPair keyPair) {
        return new OAuthPublicKeyRepository() {
            @Override
            public void refreshPublicKeys(final List<OAuthPublicKey> publicKeys) {
                // do nothing
            }

            @Override
            public List<OAuthPublicKey> retrieveActivePublicKeys() {

                final RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                final String verifierKey = "-----BEGIN PUBLIC KEY-----\n" +
                        new String(Base64.encode(publicKey.getEncoded())) +
                        "\n-----END PUBLIC KEY-----";

                final OAuthPublicKey oAuthPublicKey = OAuthPublicKey
                        .oAuthPublicKeyBuilder()
                        .withPublicKey(verifierKey)
                        .withPublicKeyFingerprint("someFingerprint")
                        .withValidFrom(now().minusHours(1))
                        .build();
                return Collections.singletonList(oAuthPublicKey);
            }
        };
    }

}
