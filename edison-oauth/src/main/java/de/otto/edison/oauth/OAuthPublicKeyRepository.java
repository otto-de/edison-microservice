package de.otto.edison.oauth;

import java.util.List;

public interface OAuthPublicKeyRepository {

    void refreshPublicKeys(List<OAuthPublicKey> publicKeys);

    List<OAuthPublicKey> retrieveActivePublicKeys();
}
