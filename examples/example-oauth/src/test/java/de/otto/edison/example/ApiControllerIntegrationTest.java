package de.otto.edison.example;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleOauthServer.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class ApiControllerIntegrationTest {
    private String baseUrl;

    @Autowired
    private AsyncHttpClient asyncHttpClient;

    @Autowired
    private OAuthTestHelper oAuthTestHelper;

    @LocalServerPort
    private int port;


    @Before
    public void setUp() {
        baseUrl = String.format("http://localhost:%d", port);
    }

    @Test
    public void shouldReturnHelloResponseWithValidOauthToken() throws Exception {
        // Given
        final String bearerToken = oAuthTestHelper.getBearerToken("hello.read");

        // When
        final Response response = asyncHttpClient
                .prepareGet(baseUrl + "/api/hello")
                .addQueryParam("context", "mode")
                .addHeader(AUTHORIZATION, bearerToken)
                .addHeader(ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .execute()
                .get();

        // Then
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_UTF8_VALUE));
        assertThat(response.getResponseBody(), is("{\"hello\": \"world\"}"));
    }

    @Test
    public void shouldReturn403WhenRequestingWithInvalidScopeInOauthToken() throws Exception {
        // Given
        final String bearerToken = oAuthTestHelper.getBearerToken("hello.write");

        // When
        final Response response = asyncHttpClient
                .prepareGet(baseUrl + "/api/hello")
                .addQueryParam("context", "mode")
                .addHeader(AUTHORIZATION, bearerToken)
                .addHeader(ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .execute()
                .get();

        // Then
        assertThat(response.getStatusCode(), is(403));
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    @Test
    public void shouldReturn403WhenRequestingWithInvalidOauthToken() throws Exception {
        // Given
        final String bearerToken = "someInvalidBearerToken";

        // When
        final Response response = asyncHttpClient
                .prepareGet(baseUrl + "/api/hello")
                .addQueryParam("context", "mode")
                .addHeader(AUTHORIZATION, bearerToken)
                .addHeader(ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .execute()
                .get();

        // Then
        assertThat(response.getStatusCode(), is(403));
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    @Test
    public void shouldReturn403WhenRequestingWithoutOauthToken() throws Exception {
        // Given

        // When
        final Response response = asyncHttpClient
                .prepareGet(baseUrl + "/api/hello")
                .addQueryParam("context", "mode")
                .addHeader(ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .execute()
                .get();

        // Then
        assertThat(response.getStatusCode(), is(403));
        assertThat(response.getContentType(), is(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }


}
