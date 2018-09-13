package de.otto.edison.oauth;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.otto.edison.oauth.OAuthPublicKey.oAuthPublicKeyBuilder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class OAuthPublicKeyStoreTest {

    private OAuthPublicKeyStore keyStore;

    private final String publicKeyUrl = "somePublicKeyUrl";

    @Mock
    private AsyncHttpClient asyncHttpClient;

    @Mock
    private OAuthPublicKeyRepository oAuthPublicKeyRepository;

    @Mock
    private BoundRequestBuilder boundRequestBuilder;

    @Mock
    private ListenableFuture<Response> future;

    @Mock
    private Response response;

    @Before
    public void setUp() {
        initMocks(this);
        keyStore = new OAuthPublicKeyStore(publicKeyUrl, asyncHttpClient, oAuthPublicKeyRepository);
    }

    @Test
    public void shouldStoreActivePublicKeysInRepository() throws ExecutionException, InterruptedException {
        // given
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime oneDayAgo = now.minusDays(1);
        final ZonedDateTime oneDayAhead = now.plusDays(1);
        final ZonedDateTime twoDaysAgo = now.minusDays(2);

        when(asyncHttpClient.prepareGet(publicKeyUrl)).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(future);
        when(future.get()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn("[\n" +
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
        final List<OAuthPublicKey> validPublicKeys = Arrays.asList(
                publicKeyOne,
                publicKeyTwo
        );

        keyStore.retrieveApiOauthPublicKey();

        //then
        verify(oAuthPublicKeyRepository).refreshPublicKeys(validPublicKeys);
    }

    @Test
    public void shoulOnlyStoreActivePublicKeysInRepository() throws ExecutionException, InterruptedException {
        // given
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime oneDayAgo = now.minusDays(1);
        final ZonedDateTime oneDayAhead = now.plusDays(1);
        final ZonedDateTime twoDaysAgo = now.minusDays(2);

        when(asyncHttpClient.prepareGet(publicKeyUrl)).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(future);
        when(future.get()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn("[\n" +
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
                "\"validUntil\": \"" + oneDayAgo.toString() + "\"\n" +
                "}\n" +
                "]");

        final OAuthPublicKey publicKeyOne = oAuthPublicKeyBuilder()
                .withPublicKey("publicKeyOne")
                .withPublicKeyFingerprint("fingerPrintOne")
                .withValidFrom(oneDayAgo)
                .withValidUntil(oneDayAhead)
                .build();
        final List<OAuthPublicKey> validPublicKeys = Arrays.asList(
                publicKeyOne
        );

        keyStore.retrieveApiOauthPublicKey();

        //then
        verify(oAuthPublicKeyRepository).refreshPublicKeys(validPublicKeys);
    }

    @Test
    public void shoulNotCallRepositoryWhenServiceResponsesWithStatusOtherThan200() throws ExecutionException, InterruptedException {
        // given
        when(asyncHttpClient.prepareGet(publicKeyUrl)).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(future);
        when(future.get()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(500);

        keyStore.retrieveApiOauthPublicKey();

        //then
        verifyZeroInteractions(oAuthPublicKeyRepository);
    }

    @Test
    public void shoulNotCallRepositoryWithEmptyKeyList() throws ExecutionException, InterruptedException {
        // given
        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime oneDayAgo = now.minusDays(1);
        final ZonedDateTime twoDaysAgo = now.minusDays(2);

        when(asyncHttpClient.prepareGet(publicKeyUrl)).thenReturn(boundRequestBuilder);
        when(boundRequestBuilder.execute()).thenReturn(future);
        when(future.get()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);
        when(response.getResponseBody()).thenReturn("[\n" +
                "{\n" +
                "\"publicKey\": \"publicKeyTwo\",\n" +
                "\"publicKeyFingerprint\": \"fingerPrintTwo\",\n" +
                "\"validFrom\": \"" + twoDaysAgo.toString() + "\",\n" +
                "\"validUntil\": \"" + oneDayAgo.toString() + "\"\n" +
                "}\n" +
                "]");

        keyStore.retrieveApiOauthPublicKey();

        //then
        verifyZeroInteractions(oAuthPublicKeyRepository);
    }

}