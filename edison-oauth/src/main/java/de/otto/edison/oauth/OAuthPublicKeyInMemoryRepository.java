package de.otto.edison.oauth;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class OAuthPublicKeyInMemoryRepository implements OAuthPublicKeyRepository {

    private final List<OAuthPublicKey> activePublicKeys = new CopyOnWriteArrayList<>();

    @Override
    public void refreshPublicKeys(final List<OAuthPublicKey> publicKeys) {
        activePublicKeys.addAll(publicKeys
                .stream()
                .filter(this::isValid)
                .filter(k -> !activePublicKeys.contains(k))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<OAuthPublicKey> retrieveActivePublicKeys() {
        return activePublicKeys.stream().filter(this::isValid).collect(Collectors.toList());
    }

    private boolean isValid(final OAuthPublicKey publicKey) {
        final ZonedDateTime now = ZonedDateTime.now();

        return now.isAfter(publicKey.getValidFrom()) &&
                (Objects.isNull(publicKey.getValidUntil()) || now.isBefore(publicKey.getValidUntil()));
    }
}
