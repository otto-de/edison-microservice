package de.otto.edison.oauth;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.util.List;

import static de.otto.edison.oauth.OAuthPublicKey.oAuthPublicKeyBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class OAuthPublicKeyStoreTest {

    private static final String PUBLIC_KEY_URL = String.format("http://localhost:%d", 8080);

    private static final ZonedDateTime now = ZonedDateTime.now();
    private static final ZonedDateTime oneDayAgo = now.minusDays(1);
    private static final ZonedDateTime oneDayAhead = now.plusDays(1);
    private static final ZonedDateTime twoDaysAgo = now.minusDays(2);

    private OAuthPublicKeyStore keyStore;

    @Mock
    private HttpClient asyncHttpClient;

    private OAuthPublicKeyRepository oAuthPublicKeyRepository = new OAuthPublicKeyInMemoryRepository();

    @Mock
    private HttpResponse response;

    @BeforeEach
    void setUp() throws InterruptedException, IOException {
        openMocks(this);
        keyStore = new OAuthPublicKeyStore(PUBLIC_KEY_URL, asyncHttpClient, oAuthPublicKeyRepository);
        withPublicKeyResponse("[\n" +
                "{\n" +
                "\"publicKey\": \"publicKeyOne\",\n" +
                "\"publicKeyFingerprint\": \"fingerPrintOne\",\n" +
                "\"validFrom\": \"" + oneDayAgo.toString() + "\",\n" +
                "\"validUntil\": \"" + oneDayAhead.toString() + "\"\n" +
                "},\n" +
                "{\n" +
                "\"publicKey\": \"publicKeyTwo\",\n" +
                "\"publicKeyFingerprint\": \"fingerPrintTwo\",\n" +
                "\"validFrom\": \"" + twoDaysAgo.toString() + "\",\n" +
                "\"validUntil\": null\n" +
                "}\n" +
                "]");

    }

    @Test
    void shouldReturnInitiallyFetchedPublicKeys() {
        //given
        keyStore.refreshPublicKeys();

        //when
        List<OAuthPublicKey> activePublicKeys = keyStore.getActivePublicKeys();

        //then
        final OAuthPublicKey publicKeyOne = oAuthPublicKeyBuilder()
                .withPublicKey("publicKeyOne")
                .withPublicKeyFingerprint("fingerPrintOne")
                .withValidFrom(oneDayAgo)
                .withValidUntil(oneDayAhead)
                .build();
        final OAuthPublicKey publicKeyTwo = oAuthPublicKeyBuilder()
                .withPublicKey("publicKeyTwo")
                .withPublicKeyFingerprint("fingerPrintTwo")
                .withValidFrom(twoDaysAgo)
                .withValidUntil(null)
                .build();

        assertThat(activePublicKeys, Matchers.contains(publicKeyOne, publicKeyTwo));
    }

    @Test
    void shouldAddKeysToInitiallyFetchedOned() throws InterruptedException, IOException {
        //given
        keyStore.refreshPublicKeys();

        //when
        withPublicKeyResponse("[\n" +
                "  {\n" +
                "    \"publicKey\": \"publicKeyThree\",\n" +
                "    \"publicKeyFingerprint\": \"fingerPrintThree\",\n" +
                "    \"validFrom\": \"" + twoDaysAgo.toString() + "\",\n" +
                "    \"validUntil\": \"" + oneDayAhead.toString() + "\"\n" +
                "  }\n" +
                "]");
        keyStore.refreshPublicKeys();

        //then
        List<OAuthPublicKey> activePublicKeys = keyStore.getActivePublicKeys();


        final OAuthPublicKey publicKeyOne = oAuthPublicKeyBuilder()
                .withPublicKey("publicKeyOne")
                .withPublicKeyFingerprint("fingerPrintOne")
                .withValidFrom(oneDayAgo)
                .withValidUntil(oneDayAhead)
                .build();
        final OAuthPublicKey publicKeyTwo = oAuthPublicKeyBuilder()
                .withPublicKey("publicKeyTwo")
                .withPublicKeyFingerprint("fingerPrintTwo")
                .withValidFrom(twoDaysAgo)
                .withValidUntil(null)
                .build();
        final OAuthPublicKey publicKeyThree = oAuthPublicKeyBuilder()
                .withPublicKey("publicKeyThree")
                .withPublicKeyFingerprint("fingerPrintThree")
                .withValidFrom(twoDaysAgo)
                .withValidUntil(oneDayAhead)
                .build();

        assertThat(activePublicKeys, Matchers.containsInAnyOrder(publicKeyOne, publicKeyTwo, publicKeyThree));
    }

    private void withPublicKeyResponse(String publicKeyData) throws InterruptedException, IOException {
        when(asyncHttpClient.send(any(HttpRequest.class), any())).thenReturn(response);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(publicKeyData);
    }
}
