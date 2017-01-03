package de.otto.edison.registry.client;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import de.otto.edison.annotations.Beta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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
@ConditionalOnProperty("edison.servicediscovery.servers")
@ConditionalOnClass(AsyncHttpClient.class)
@Beta
public class AsyncHttpRegistryClient implements RegistryClient {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncHttpRegistryClient.class);

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private AsyncHttpClient httpClient;
    @Value("${edison.servicediscovery.servers}")
    private String discoveryServers;
    @Value("${edison.servicediscovery.service}")
    private String serviceUrl;
    @Value("${edison.servicediscovery.expire-after:15}")
    private long expireAfterMinutes;
    @Value("${edison.servicediscovery.refresh-after:5}")
    private long refreshAfterMinutes;
    @Value("${edison.status.application.environment:unknown}")
    private String applicationEnvironment;
    @Value("${edison.status.application.group:default}")
    private String applicationGroup;

    @PostConstruct
    public void postConstruct() {
        LOG.info("Scheduling registration at Edison JobTrigger every '{}' minutes.", refreshAfterMinutes);
        newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this::registerService, 0, refreshAfterMinutes, MINUTES);
    }

    @Override
    public void registerService() {
        stream(discoveryServers.split(","))
                .filter(server -> !isEmpty(server))
                .forEach(discoveryServer -> {
                    try {
                        LOG.debug("Updating registration of service at '{}'", discoveryServer);
                        httpClient
                                .preparePut(discoveryServer + "/environments/" + applicationEnvironment + "/" + applicationName)
                                .setHeader("Content-Type", "application/vnd.otto.edison.links+json")
                                .setHeader("Accept", "application/vnd.otto.edison.links+json")
                                .setBody(
                                        "{\n" +
                                                "   \"groups\":[\"" + applicationGroup + "\"],\n" +
                                                "   \"expire\":" + expireAfterMinutes + ",\n" +
                                                "   \"links\":[{\n" +
                                                "      \"rel\":\"http://github.com/otto-de/edison/link-relations/microservice\",\n" +
                                                "      \"href\" : \"" + serviceUrl + "\",\n" +
                                                "      \"title\":\"" + applicationName + "\"\n" +
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

}
