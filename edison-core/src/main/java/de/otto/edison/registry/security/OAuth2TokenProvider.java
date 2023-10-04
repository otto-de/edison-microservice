package de.otto.edison.registry.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class OAuth2TokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OAuth2TokenProvider.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String clientId;
    private final String clientSecret;
    private final String tokenEndpoint;
    private final int timeoutSeconds;

    public OAuth2TokenProvider(final String clientId,
                               final String clientSecret,
                               final String tokenEndpoint,
                               final int timeoutSeconds) {
        this.httpClient = HttpClient.newBuilder().build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tokenEndpoint = tokenEndpoint;
        this.timeoutSeconds = timeoutSeconds;
        this.objectMapper = new ObjectMapper();
    }

    public String getAccessToken() throws ExecutionException, InterruptedException, JsonProcessingException, OAuth2TokenException {
        try {
            final HttpResponse<String> response = httpClient
                    .sendAsync(HttpRequest.newBuilder()
                            .timeout(Duration.ofSeconds(timeoutSeconds))
                            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret))
                            .uri(URI.create(tokenEndpoint))
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .build(), HttpResponse.BodyHandlers.ofString())
                    .get();
            if (response.statusCode() != HttpStatus.OK.value()) {
                throw new OAuth2TokenException();
            }
            return objectMapper.readValue(response.body(), OAuth2TokenResponse.class).accessToken();
        } catch (final Exception e) {
            LOG.error("Error fetching access token", e);
            throw e;
        }
    }
}
