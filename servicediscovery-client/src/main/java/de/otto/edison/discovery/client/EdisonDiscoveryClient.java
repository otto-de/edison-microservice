package de.otto.edison.discovery.client;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static java.util.Arrays.stream;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Simple Edison implementation of a DiscoveryClient.
 *
 * @author Guido Steinacker
 * @since 16.09.15
 */
@Component
@ConditionalOnProperty("edison.servicediscovery.servers")
public class EdisonDiscoveryClient implements DiscoveryClient {

    private static final Logger LOG = LoggerFactory.getLogger(EdisonDiscoveryClient.class);

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
    @Value("${edison.servicediscovery.environment:unknown}")
    private String applicationEnvironment;
    @Value("${edison.servicediscovery.group:default}")
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
