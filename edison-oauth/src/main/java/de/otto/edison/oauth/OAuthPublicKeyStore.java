package de.otto.edison.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
@ConditionalOnExpression("${api.oauth.public-key.enabled:false} && ${api.oauth.public-key.interval:0}>0")
public class OAuthPublicKeyStore {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthPublicKeyStore.class);
    private final ObjectMapper objectMapper;
    private final String publicKeyUrl;
    private final AsyncHttpClient asyncHttpClient;
    private final OAuthPublicKeyRepository oAuthPublicKeyRepository;

    @Autowired
    public OAuthPublicKeyStore(@Value("${api.oauth.public-key.url}") final String publicKeyUrl,
                               final AsyncHttpClient asyncHttpClient,
                               final OAuthPublicKeyRepository oAuthPublicKeyRepository) {
        this.publicKeyUrl = publicKeyUrl;
        this.oAuthPublicKeyRepository = oAuthPublicKeyRepository;
        this.asyncHttpClient = asyncHttpClient;

        this.objectMapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
        objectMapper.registerModule(module);
    }

    @Scheduled(fixedDelayString = "${api.oauth.public-key.interval}")
    public void retrieveApiOauthPublicKey() {
        try {
            final Response response = asyncHttpClient
                    .prepareGet(publicKeyUrl)
                    .setRequestTimeout(5000)
                    .execute()
                    .get();
            if (response.getStatusCode() == HttpStatus.OK.value()) {
                try {
                    final OAuthPublicKey[] retrievedPublicKeys = objectMapper.readValue(response.getResponseBody(), OAuthPublicKey[].class);
                    final List<OAuthPublicKey> activePublicKeys = filterActivePublicKeys(retrievedPublicKeys);

                    if (activePublicKeys.isEmpty()) {
                        LOG.error(String.format("Did not retrieve valid OAuthPublicKeys from %s", publicKeyUrl));
                    } else {
                        oAuthPublicKeyRepository.refreshPublicKeys(activePublicKeys);
                        LOG.info(String.format(
                                "Successfully retrieved %d public keys with the following finger prints: %s",
                                activePublicKeys.size(),
                                activePublicKeys.stream().map(OAuthPublicKey::getPublicKeyFingerprint).collect(toList()))
                        );
                    }
                } catch (final IOException ex) {
                    LOG.error(String.format("Unable to parse PublicKeys from OAuth-Server-Response: %s", ex.getMessage()), ex);
                }
            } else {
                LOG.error(String.format("Unable to retrieve list of public keys. Status was %d", response.getStatusCode()));
            }
        } catch (final InterruptedException | ExecutionException ex) {
            LOG.error("Unable to retrieve list of public keys.", ex);
        }
    }

    private List<OAuthPublicKey> filterActivePublicKeys(final OAuthPublicKey[] publicKeys) {
        return Arrays
                .stream(publicKeys)
                .filter(this::isValid)
                .collect(toList());
    }

    private boolean isValid(final OAuthPublicKey publicKey) {
        final ZonedDateTime now = ZonedDateTime.now();

        return now.isAfter(publicKey.getValidFrom()) &&
                (Objects.isNull(publicKey.getValidUntil()) || now.isBefore(publicKey.getValidUntil()));
    }
}
