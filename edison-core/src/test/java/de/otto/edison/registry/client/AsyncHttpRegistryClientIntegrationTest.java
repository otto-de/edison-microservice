package de.otto.edison.registry.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.registry.configuration.ServiceRegistryProperties;
import de.otto.edison.registry.security.OAuth2TokenException;
import de.otto.edison.registry.security.OAuth2TokenProvider;
import de.otto.edison.registry.security.OAuth2TokenProviderFactory;
import org.apache.http.HttpHeaders;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static de.otto.edison.status.domain.ApplicationInfo.applicationInfo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = {1080})
public class AsyncHttpRegistryClientIntegrationTest {

    @Mock
    private ServiceRegistryProperties serviceRegistryProperties;

    @Mock
    private EdisonApplicationProperties edisonApplicationProperties;

    @Mock
    private OAuth2TokenProviderFactory oAuth2TokenProviderFactory;

    @BeforeEach
    public void setUp(MockServerClient client) {
        client.reset();
        when(edisonApplicationProperties.getEnvironment()).thenReturn("testEnvironment");
        when(serviceRegistryProperties.getServers()).thenReturn("http://localhost:1080/serviceregistry");
    }

    @Test
    public void shouldCallRegistryEndpointWithoutAuthorization(MockServerClient client) {
        // given
        final var asyncHttpRegistryClient = new AsyncHttpRegistryClient(applicationInfo("testApplication", edisonApplicationProperties),
                serviceRegistryProperties, edisonApplicationProperties, oAuth2TokenProviderFactory);
        asyncHttpRegistryClient.postConstruct();
        client.when(
                request("/serviceregistry/environments/testEnvironment/testApplication")
                        .withMethod("PUT")
        ).respond(
                response().withStatusCode(HttpStatus.OK.value())
        );

        // when
        asyncHttpRegistryClient.registerService();

        // then
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .ignoreExceptions()
                .until(() -> {
                    client.verify(request("/serviceregistry/environments/testEnvironment/testApplication")
                            .withMethod("PUT"));
                    return true;
                });

        assertFalse(
                Arrays.stream(client.retrieveRecordedRequests(request("/serviceregistry/environments/testEnvironment/testApplication").withMethod("PUT")))
                        .anyMatch(httpRequest -> httpRequest.containsHeader(HttpHeaders.AUTHORIZATION)));
    }

    @Test
    public void shouldCallRegistryEndpointWithAuthorization(MockServerClient client) throws OAuth2TokenException, ExecutionException, InterruptedException, JsonProcessingException {
        // given
        final var tokenProvider = mock(OAuth2TokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("someAccessToken");
        when(serviceRegistryProperties.isEnabled()).thenReturn(true);
        when(serviceRegistryProperties.getService()).thenReturn("someService");
        when(serviceRegistryProperties.getRefreshAfter()).thenReturn(123L);
        when(oAuth2TokenProviderFactory.isEnabled()).thenReturn(true);
        when(oAuth2TokenProviderFactory.create()).thenReturn(tokenProvider);

        final var asyncHttpRegistryClient = new AsyncHttpRegistryClient(applicationInfo("testApplication", edisonApplicationProperties),
                serviceRegistryProperties, edisonApplicationProperties, oAuth2TokenProviderFactory);
        asyncHttpRegistryClient.postConstruct();

        client.when(
                request("/serviceregistry/environments/testEnvironment/testApplication")
                        .withMethod("PUT")
        ).respond(
                response().withStatusCode(HttpStatus.OK.value())
        );

        // when
        asyncHttpRegistryClient.registerService();

        // then
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .ignoreExceptions()
                .until(() -> {
                    client.verify(request("/serviceregistry/environments/testEnvironment/testApplication")
                            .withMethod("PUT")
                            .withHeader(HttpHeaders.AUTHORIZATION, "Bearer someAccessToken"));
                    return true;
                });
    }

    @Test
    public void shouldNotThrowExceptionOnFailingTokenCall() throws OAuth2TokenException, ExecutionException, InterruptedException, JsonProcessingException {
        // given
        final var tokenProvider = mock(OAuth2TokenProvider.class);
        when(tokenProvider.getAccessToken()).thenThrow(OAuth2TokenException.class);
        when(serviceRegistryProperties.isEnabled()).thenReturn(true);
        when(serviceRegistryProperties.getService()).thenReturn("someService");
        when(serviceRegistryProperties.getRefreshAfter()).thenReturn(123L);
        when(oAuth2TokenProviderFactory.isEnabled()).thenReturn(true);
        when(oAuth2TokenProviderFactory.create()).thenReturn(tokenProvider);

        final var asyncHttpRegistryClient = new AsyncHttpRegistryClient(applicationInfo("testApplication", edisonApplicationProperties),
                serviceRegistryProperties, edisonApplicationProperties, oAuth2TokenProviderFactory);
        asyncHttpRegistryClient.postConstruct();

        // when
        asyncHttpRegistryClient.registerService();

        // then
        // no exception thrown
    }

}
