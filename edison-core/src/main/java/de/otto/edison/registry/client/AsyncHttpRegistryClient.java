package de.otto.edison.registry.client;

import de.otto.edison.annotations.Beta;
import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.registry.configuration.ServiceRegistryProperties;
import de.otto.edison.status.domain.ApplicationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Arrays.stream;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
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
    private final ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor();
    private boolean isRunning = false;

    @Autowired
    public AsyncHttpRegistryClient(final ApplicationInfo applicationInfo,
                                   final ServiceRegistryProperties serviceRegistryProperties,
                                   final EdisonApplicationProperties edisonApplicationProperties) {
        this.applicationInfo = applicationInfo;
        this.httpClient = HttpClient.newBuilder().build();
        this.serviceRegistryProperties = serviceRegistryProperties;
        this.edisonApplicationProperties = edisonApplicationProperties;
    }

    @PostConstruct
    public void postConstruct() {
        if (serviceRegistryProperties.isEnabled()) {
            if (validateConfig()) {
                LOG.info("Scheduling registration at Edison JobTrigger every '{}' minutes.", serviceRegistryProperties.getRefreshAfter());
                scheduledExecutorService
                        .scheduleWithFixedDelay(this::registerService, 0, serviceRegistryProperties.getRefreshAfter(), MINUTES);
                isRunning = true;
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
                        LOG.debug("Updating registration of service at '{}'", discoveryServer);
                        httpClient
                                .sendAsync(HttpRequest.newBuilder()
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
                                        .header("Content-Type", "application/vnd.otto.edison.links+json")
                                        .header("Accept", "application/vnd.otto.edison.links+json")
                                        .build(), HttpResponse.BodyHandlers.ofString())
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
