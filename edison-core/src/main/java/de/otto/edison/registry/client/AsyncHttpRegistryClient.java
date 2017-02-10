package de.otto.edison.registry.client;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import de.otto.edison.annotations.Beta;
import de.otto.edison.registry.configuration.ServiceRegistryProperties;
import de.otto.edison.status.configuration.ApplicationInfoProperties;
import de.otto.edison.status.domain.ApplicationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

import java.util.concurrent.ScheduledExecutorService;

import static java.util.Arrays.stream;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Simple implementation of a RegistryClient using AsyncHttpClient to register the service.
 * <p>
 *     In order to use this class, a dependency to com.ning:async-http-client:1.9.40 or later must be added to your build.
 * </p>
 *
 * @author Guido Steinacker
 * @since 1.0.0
 */
@Component
@ConditionalOnClass(AsyncHttpClient.class)
@EnableConfigurationProperties(ServiceRegistryProperties.class)
@Beta
public class AsyncHttpRegistryClient implements RegistryClient {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncHttpRegistryClient.class);

    private final ApplicationInfo applicationInfo;
    private final AsyncHttpClient httpClient;
    private final ServiceRegistryProperties serviceRegistryProperties;
    private final ApplicationInfoProperties applicationInfoProperties;
    private final ScheduledExecutorService scheduledExecutorService = newSingleThreadScheduledExecutor();
    private boolean isRunning = false;

    @Autowired
    public AsyncHttpRegistryClient(final ApplicationInfo applicationInfo,
                                   final AsyncHttpClient httpClient,
                                   final ServiceRegistryProperties serviceRegistryProperties,
                                   final ApplicationInfoProperties applicationInfoProperties) {
        this.applicationInfo = applicationInfo;
        this.httpClient = httpClient;
        this.serviceRegistryProperties = serviceRegistryProperties;
        this.applicationInfoProperties = applicationInfoProperties;
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
                                .preparePut(discoveryServer + "/environments/" + applicationInfoProperties.getEnvironment() + "/" + applicationInfo.name)
                                .setHeader("Content-Type", "application/vnd.otto.edison.links+json")
                                .setHeader("Accept", "application/vnd.otto.edison.links+json")
                                .setBody(
                                        "{\n" +
                                                "   \"groups\":[\"" + applicationInfoProperties.getGroup() + "\"],\n" +
                                                "   \"expire\":" + serviceRegistryProperties.getExpireAfter() + ",\n" +
                                                "   \"links\":[{\n" +
                                                "      \"rel\":\"http://github.com/otto-de/edison/link-relations/microservice\",\n" +
                                                "      \"href\" : \"" + serviceRegistryProperties.getService() + "\",\n" +
                                                "      \"title\":\"" + applicationInfo.name + "\"\n" +
                                                "   }]  \n" +
                                                "}"
                                )
                                .execute(new AsyncCompletionHandler<Integer>() {
                                    @Override
                                    public Integer onCompleted(final Response response) throws Exception {
                                        if (response.getStatusCode() < 300) {
                                            LOG.info("Successfully updated registration at " + discoveryServer);
                                        } else {
                                            LOG.warn("Failed to update registration at '{}': Status='{}' '{}'", discoveryServer, response.getStatusCode(), response.getStatusText());
                                        }
                                        return response.getStatusCode();
                                    }

                                    @Override
                                    public void onThrowable(final Throwable t) {
                                        LOG.error("Failed to register at '{}'", discoveryServer, t);
                                    }
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
