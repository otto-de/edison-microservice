package de.otto.edison.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnExpression("${edison.oauth.public-key.enabled:false} && ${edison.oauth.public-key.interval:0}>0")
public class OAuthPublicKeyStore {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthPublicKeyStore.class);
    private final ObjectMapper objectMapper;
    private final String publicKeyUrl;
    private final HttpClient httpClient;
    private final OAuthPublicKeyRepository oAuthPublicKeyRepository;
    private final CountDownLatch publicKeysRetrievedCountDownLatch = new CountDownLatch(1);

    @Autowired
    public OAuthPublicKeyStore(@Value("${edison.oauth.public-key.url}") final String publicKeyUrl,
                               final HttpClient asyncHttpClient,
                               final OAuthPublicKeyRepository oAuthPublicKeyRepository) {
        this.publicKeyUrl = publicKeyUrl;
        this.oAuthPublicKeyRepository = oAuthPublicKeyRepository;
        this.httpClient = asyncHttpClient;

        this.objectMapper = createObjectMapper();
    }

    public List<OAuthPublicKey> getActivePublicKeys() {
        try {
            if (publicKeysRetrievedCountDownLatch.await(10, TimeUnit.SECONDS)) {
                return oAuthPublicKeyRepository.retrieveActivePublicKeys();
            } else {
                throw new RuntimeException("Timeout while waiting that public keys got fetched");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Got interrupted while waiting that public keys got fetched");
        }
    }

    @Scheduled(fixedDelayString = "${edison.oauth.public-key.interval}")
    public void refreshPublicKeys() {
        LOG.info("Start refreshing public keys");
        List<OAuthPublicKey> oAuthPublicKeys = fetchOAuthPublicKeysFromServer();
        oAuthPublicKeyRepository.refreshPublicKeys(oAuthPublicKeys);
        publicKeysRetrievedCountDownLatch.countDown();
        LOG.info("Done refreshing public keys");

    }

    List<OAuthPublicKey> fetchOAuthPublicKeysFromServer() {
        try {
            final HttpResponse response = httpClient
                    .send(HttpRequest.newBuilder()
                            .GET()
                            .uri(URI.create(publicKeyUrl))
                            .timeout(Duration.ofMillis(5000))
                            .build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpStatus.OK.value()) {
                return objectMapper.readValue(response.body().toString(), TypeFactory.defaultInstance().constructCollectionType(List.class, OAuthPublicKey.class));
            } else {
                LOG.warn("Unable to retrieve list of public keys. Got status code {}", response.statusCode());
            }
        } catch (IOException e) {
            LOG.error("Unable to retrieve list of public keys. ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Collections.emptyList();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper newObjectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
        newObjectMapper.registerModule(module);
        return newObjectMapper;
    }

}

