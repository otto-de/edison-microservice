package de.otto.edison.oauth;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.otto.edison.oauth.OAuthPublicKey.oAuthPublicKeyBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class OAuthPublicKeyStoreTest {

    private static final String PUBLIC_KEY_URL = "somePublicKeyUrl";

    private static final ZonedDateTime now = ZonedDateTime.now();
    private static final ZonedDateTime oneDayAgo = now.minusDays(1);
    private static final ZonedDateTime oneDayAhead = now.plusDays(1);
    private static final ZonedDateTime twoDaysAgo = now.minusDays(2);

    private OAuthPublicKeyStore keyStore;

    @Mock
    private AsyncHttpClient asyncHttpClient;


    @Mock
    private BoundRequestBuilder boundRequestBuilder;

    @Mock
    private ListenableFuture<Response> future;

    private OAuthPublicKeyRepository oAuthPublicKeyRepository = new OAuthPublicKeyInMemoryRepository();

    @Mock
    private Response response;

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException {
        initMocks(this);
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
    void shouldAddKeysToInitiallyFetchedOned() throws ExecutionException, InterruptedException {
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

    private void withPublicKeyResponse(String publicKeyData) throws InterruptedException, ExecutionException {
        when(asyncHttpClient.prepareGet(PUBLIC_KEY_URL)).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.setRequestTimeout(anyInt())).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(future);
        when(future.get()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn(publicKeyData);
    }

}