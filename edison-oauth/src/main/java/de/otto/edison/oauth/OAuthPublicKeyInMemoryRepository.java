package de.otto.edison.oauth;

import org.springframework.util.Assert;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OAuthPublicKeyInMemoryRepository implements OAuthPublicKeyRepository {

    private final List<OAuthPublicKey> activePublicKeys = new ArrayList<>();

    @Override
    public void refreshPublicKeys(final List<OAuthPublicKey> publicKeys) throws IllegalArgumentException {

        Assert.isTrue(publicKeys
                .stream()
                .allMatch(this::isValid), "Invalid public keys retrieved");

        activePublicKeys.addAll(publicKeys
                .stream()
                .filter(k -> !activePublicKeys.contains(k))
                .collect(Collectors.toList())
        );
    }

    private boolean isValid(final OAuthPublicKey publicKey) {
        final ZonedDateTime now = ZonedDateTime.now();

        return now.isAfter(publicKey.getValidFrom()) &&
                (Objects.isNull(publicKey.getValidUntil()) || now.isBefore(publicKey.getValidUntil()));
    }

    @Override
    public List<OAuthPublicKey> retrieveActivePublicKeys() {
        return activePublicKeys.stream().filter(this::isValid).collect(Collectors.toList());
    }
}
