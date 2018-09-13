package de.otto.edison.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class KeyExchangeJwtAccessTokenConverter extends JwtAccessTokenConverter {

    private static final Logger LOG = LoggerFactory.getLogger(KeyExchangeJwtAccessTokenConverter.class);
    private static final JsonParser objectMapper = JsonParserFactory.create();
    private final OAuthPublicKeyRepository oAuthPublicKeyRepository;

    @Autowired
    public KeyExchangeJwtAccessTokenConverter(final OAuthPublicKeyRepository oAuthPublicKeyRepository) {

        this.oAuthPublicKeyRepository = oAuthPublicKeyRepository;
    }

    @Override
    protected Map<String, Object> decode(final String token) {
        final List<OAuthPublicKey> currentPublicKeys = oAuthPublicKeyRepository.retrieveActivePublicKeys();

        for (final OAuthPublicKey publicKey : currentPublicKeys) {
            try {
                return decodeJwtMap(token, publicKey);
            } catch (final Exception e) {
                LOG.error(String.format("Unable to verify JWT token with public key: %s", publicKey.getPublicKeyFingerprint()));
            }
        }
        return Collections.emptyMap();
    }

    private Map<String, Object> decodeJwtMap(final String token, final OAuthPublicKey keyExchangePublicKey) {
        final RsaVerifier rsaVerifier = new RsaVerifier(keyExchangePublicKey.getPublicKey());
        final Jwt jwt = JwtHelper.decodeAndVerify(token, rsaVerifier);

        final String content = jwt.getClaims();

        final Map<String, Object> map = objectMapper.parseMap(content);
        if (map.containsKey(EXP) && map.get(EXP) instanceof Integer) {
            final Integer intValue = (Integer) map.get(EXP);
            map.put(EXP, Long.valueOf(intValue));
        }
        return map;
    }
}

