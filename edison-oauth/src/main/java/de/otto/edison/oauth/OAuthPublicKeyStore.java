package de.otto.edison.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Component
@ConditionalOnMissingBean(OAuthPublicKeyStore.class)
@ConditionalOnProperty(prefix = "api.oauth.public-key", name = "enabled", havingValue = "true")
public class OAuthPublicKeyStore {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthPublicKeyStore.class);
    private final ObjectMapper objectMapper;
    private final String publicKeyUrl;
    private final AsyncHttpClient asyncHttpClient;
    private final OAuthPublicKeyRepository oAuthPublicKeyRepository;

    @Autowired
    public OAuthPublicKeyStore(@Value("${api.oauth.publicKey.url}") final String publicKeyUrl,
                               final AsyncHttpClient asyncHttpClient,
                               final OAuthPublicKeyRepository oAuthPublicKeyRepository) {
        this.publicKeyUrl = publicKeyUrl;
        this.asyncHttpClient = asyncHttpClient;
        this.oAuthPublicKeyRepository = oAuthPublicKeyRepository;
        this.objectMapper = new ObjectMapper();

        final SimpleModule module = new SimpleModule();
        module.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());
        objectMapper.registerModule(module);
    }

    @Scheduled(fixedDelayString = "${api.oauth.interval}")
    public void retrieveApiOauthPublicKey() {
        try {
            final Response response = asyncHttpClient.prepareGet(publicKeyUrl).execute().get();
            if (response.getStatusCode() == HttpStatus.OK.value()) {
                try {
                    final OAuthPublicKey[] retrievedPublicKeys = objectMapper.readValue(response.getResponseBody(), OAuthPublicKey[].class);
                    final List<OAuthPublicKey> activePublicKeys = filterActivePublicKeys(retrievedPublicKeys);

                    if (activePublicKeys.isEmpty()) {
                        LOG.error(String.format("Did not retrieve valid OAuthPublicKeys from %s", publicKeyUrl));
                    } else {
                        oAuthPublicKeyRepository.refreshPublicKeys(activePublicKeys);
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
                .collect(Collectors.toList());
    }

    private boolean isValid(final OAuthPublicKey publicKey) {
        final ZonedDateTime now = ZonedDateTime.now();

        return now.isAfter(publicKey.getValidFrom()) &&
                (Objects.isNull(publicKey.getValidUntil()) || now.isBefore(publicKey.getValidUntil()));
    }
}
