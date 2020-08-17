package de.otto.edison.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ExampleOauthServer.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class ApiControllerIntegrationTest {

    private String baseUrl;

    @Autowired
    private HttpClient asyncHttpClient;

    @Autowired
    private OAuthTestHelper oAuthTestHelper;

    @LocalServerPort
    private int port;


    @BeforeEach
    public void setUp() {
        baseUrl = String.format("http://localhost:%d", port);
    }

    @Test
    public void shouldReturnHelloResponseWithValidOauthToken() throws Exception {
        // Given
        final String bearerToken = oAuthTestHelper.getBearerToken("hello.read");

        // When
        final HttpResponse response = asyncHttpClient
                .send(HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(baseUrl + "/api/hello?context=mode"))
                        .header(AUTHORIZATION, bearerToken)
                        .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .build(), HttpResponse.BodyHandlers.ofString());

        // Then
        assertThat(response.statusCode(), is(200));
        assertThat(response.body(), is("{\"hello\": \"world\"}"));
    }

    @Test
    public void shouldReturn403WhenRequestingWithInvalidScopeInOauthToken() throws Exception {
        // Given
        final String bearerToken = oAuthTestHelper.getBearerToken("hello.write");

        // When
        final HttpResponse response = asyncHttpClient
                .send(HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(baseUrl + "/api/hello?context=mode"))
                        .header(AUTHORIZATION, bearerToken)
                        .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .build(), HttpResponse.BodyHandlers.ofString());

        // Then
        assertThat(response.statusCode(), is(403));

        String expectedJson = "{\n" +
                "  \"error_description\" : \"Insufficient scope for this resource\",\n" +
                "  \"error\" : \"forbidden\"\n" +
                "}";
        JSONAssert.assertEquals(expectedJson, response.body().toString(), false);
    }

    @Test
    public void shouldReturn403WhenRequestingWithInvalidOauthToken() throws Exception {
        // Given
        final String bearerToken = "someInvalidBearerToken";

        // When
        final HttpResponse response = asyncHttpClient
                .send(HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(baseUrl + "/api/hello?context=mode"))
                        .header(AUTHORIZATION, bearerToken)
                        .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .build(), HttpResponse.BodyHandlers.ofString());

        // Then
        assertThat(response.statusCode(), is(403));
        String expectedJson = "{\n" +
                "  \"error_description\" : \"Insufficient scope for this resource\",\n" +
                "  \"error\" : \"forbidden\"\n" +
                "}";
        JSONAssert.assertEquals(expectedJson, response.body().toString(), false);
    }

    @Test
    public void shouldReturn403WhenRequestingWithoutOauthToken() throws Exception {
        // Given

        // When
        final HttpResponse response = asyncHttpClient
                .send(HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(baseUrl + "/api/hello?context=mode"))
                        .header(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                        .build(), HttpResponse.BodyHandlers.ofString());

        // Then
        assertThat(response.statusCode(), is(403));
        String expectedJson = "{\n" +
                "  \"error_description\" : \"Insufficient scope for this resource\",\n" +
                "  \"error\" : \"forbidden\"\n" +
                "}";
        JSONAssert.assertEquals(expectedJson, response.body().toString(), false);
    }
}
