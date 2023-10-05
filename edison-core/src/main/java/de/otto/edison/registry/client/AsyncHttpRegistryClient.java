package de.otto.edison.registry.client;

import de.otto.edison.annotations.Beta;
import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.registry.configuration.ServiceRegistryProperties;
import de.otto.edison.registry.security.OAuth2TokenProvider;
import de.otto.edison.registry.security.OAuth2TokenProviderFactory;
import de.otto.edison.status.domain.ApplicationInfo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Arrays.stream;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Simple implementation of a RegistryClient using HttpClient to register the service.
 *
 * @author Guido Steinacker
 * @since 1.0.0
 */
@Component
@EnableConfigurationProperties(ServiceRegistryProperties.class)
@Beta
public class AsyncHttpRegistryClient implements RegistryClient {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncHttpRegistryClient.class);
    private final ApplicationInfo applicationInfo;
    private final HttpClient httpClient;
    private final ServiceRegistryProperties serviceRegistryProperties;
    private final EdisonApplicationProperties edisonApplicationProperties;
    private final OAuth2TokenProviderFactory oAuth2TokenProviderFactory;
    private OAuth2TokenProvider oAuth2TokenProvider;
    private final ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor();
    private boolean isRunning = false;

    @Autowired
    public AsyncHttpRegistryClient(final ApplicationInfo applicationInfo,
                                   final ServiceRegistryProperties serviceRegistryProperties,
                                   final EdisonApplicationProperties edisonApplicationProperties,
                                   final OAuth2TokenProviderFactory oAuth2TokenProviderFactory) {
        this.applicationInfo = applicationInfo;
        this.httpClient = HttpClient.newBuilder().build();
        this.serviceRegistryProperties = serviceRegistryProperties;
        this.edisonApplicationProperties = edisonApplicationProperties;
        this.oAuth2TokenProviderFactory = oAuth2TokenProviderFactory;
    }

    @PostConstruct
    public void postConstruct() {
        if (serviceRegistryProperties.isEnabled()) {
            if (validateConfig()) {
                LOG.info("Scheduling registration at Edison JobTrigger every '{}' minutes.", serviceRegistryProperties.getRefreshAfter());
                scheduledExecutorService
                        .scheduleWithFixedDelay(this::registerService, 0, serviceRegistryProperties.getRefreshAfter(), MINUTES);
                isRunning = true;
                oAuth2TokenProvider = oAuth2TokenProviderFactory.isEnabled() ? oAuth2TokenProviderFactory. create() : null;
            } else {
                LOG.warn("===================================================================================");
                LOG.warn("ServiceRegistryProperties is enabled, but no service and/or servers are configured");
                LOG.warn(serviceRegistryProperties.toString());
                LOG.warn("===================================================================================");
            }
        } else {
            LOG.info("Scheduling registration at Edison JobTrigger disabled!");
        }
    }

    @Override
    public void registerService() {
        stream(serviceRegistryProperties.getServers().split(","))
                .filter(server -> !isEmpty(server))
                .forEach(discoveryServer -> {
                    try {
                        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                                .PUT(HttpRequest.BodyPublishers.ofString(
                                        "{\n" +
                                        "   \"groups\":[\"" + edisonApplicationProperties.getGroup() + "\"],\n" +
                                        "   \"expire\":" + serviceRegistryProperties.getExpireAfter() + ",\n" +
                                        "   \"links\":[{\n" +
                                        "      \"rel\":\"http://github.com/otto-de/edison/link-relations/microservice\",\n" +
                                        "      \"href\" : \"" + serviceRegistryProperties.getService() + "\",\n" +
                                        "      \"title\":\"" + applicationInfo.title + "\"\n" +
                                        "   }]  \n" +
                                        "}"
                                ))
                                .uri(URI.create(discoveryServer + "/environments/" + edisonApplicationProperties.getEnvironment() + "/" + applicationInfo.name))
                                .header(CONTENT_TYPE, "application/vnd.otto.edison.links+json")
                                .header(ACCEPT, "application/vnd.otto.edison.links+json");

                        if (oAuth2TokenProvider != null) {
                            requestBuilder.header(AUTHORIZATION, "Bearer " + oAuth2TokenProvider.getAccessToken());
                        }

                        LOG.debug("Updating registration of service at '{}'", discoveryServer);
                        httpClient
                                .sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                                .thenApply(response -> {
                                    if (response.statusCode() < 300) {
                                        LOG.info("Successfully updated registration at " + discoveryServer);
                                    } else {
                                        LOG.warn("Failed to update registration at '{}': Status='{}'", discoveryServer, response.statusCode());
                                    }
                                    return response.statusCode();
                                });
                    } catch (final Exception e) {
                        LOG.error("Error updating registration", e);
                    }
                });
    }

    private boolean validateConfig() {
        if (!serviceRegistryProperties.isEnabled()) {
            return true;
        }

        if (isEmpty(serviceRegistryProperties.getServers())) {
            return false;
        }

        if (isEmpty(serviceRegistryProperties.getService())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
