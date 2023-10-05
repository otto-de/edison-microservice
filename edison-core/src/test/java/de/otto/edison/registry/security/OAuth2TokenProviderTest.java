package de.otto.edison.registry.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.springframework.http.HttpStatus;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {1080})
class OAuth2TokenProviderTest {

    private final String clientId = "someClientId";
    private final String clientSecret = "someClientSecret";
    private final String tokenEndpoint = "http://localhost:1080/token";
    private OAuth2TokenProvider tokenProvider;

    @BeforeEach
    void setUp(MockServerClient client) {
        client.reset();
        tokenProvider = new OAuth2TokenProvider(clientId, clientSecret, tokenEndpoint, 10);
    }

    @Test
    public void shouldGetAccessToken(MockServerClient client) throws OAuth2TokenException, ExecutionException, InterruptedException, JsonProcessingException {
        // given
        client.when(
                request("/token")
                        .withMethod("POST")
        ).respond(
                response()
                        .withStatusCode(HttpStatus.OK.value())
                        .withBody(""" 
                                { "access_token": "someAccessToken", "expires_in": 1000 }
                                """));

        // when
        final var token = tokenProvider.getAccessToken();

        // then
        client.verify(request("/token")
                .withMethod("POST")
                .withBody("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret));
        assertEquals(token, "someAccessToken");
    }

    @Test
    public void shouldThrowExceptionOnBadStatusCode(MockServerClient client) {
        // given
        client.when(
                request("/token")
                        .withMethod("POST")
        ).respond(
                response()
                        .withStatusCode(HttpStatus.BAD_REQUEST.value()));

        // when + then
        assertThrows(OAuth2TokenException.class, tokenProvider::getAccessToken);
    }


    @Test
    public void shouldThrowExceptionOnUnexpectedJson(MockServerClient client) {
        // given
        client.when(
                request("/token")
                        .withMethod("POST")
        ).respond(
                response()
                        .withStatusCode(HttpStatus.OK.value())
                        .withBody(""" 
                                { "isThisBroken": "yes it is!" }
                                """));

        // when + then
        assertThrows(JsonProcessingException.class, tokenProvider::getAccessToken);
    }

}
