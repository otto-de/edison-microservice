package de.otto.edison.oauth;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.otto.edison.oauth.OAuthPublicKey.oAuthPublicKeyBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OAuthPublicKeyInMemoryRepositoryTest {


    private OAuthPublicKeyInMemoryRepository inMemoryRepository;

    @Before
    public void setUp() {
        inMemoryRepository = new OAuthPublicKeyInMemoryRepository();
    }

    @Test
    public void shouldUpdateCachedListWithValidKeys() {
        // given
        final ZonedDateTime now = ZonedDateTime.now();
        final OAuthPublicKey publicKeyOne = oAuthPublicKeyBuilder()
                .withPublicKey("publicKeyOne")
                .withPublicKeyFingerprint("fingerPrintOne")
                .withValidFrom(now.minusDays(1))
                .withValidUntil(now.plusDays(1))
                .build();
        final OAuthPublicKey publicKeyTwo = oAuthPublicKeyBuilder()
                .withPublicKey("publicKeyTwo")
                .withPublicKeyFingerprint("fingerPrintTwo")
                .withValidFrom(now.minusDays(2))
                .withValidUntil(now.plusDays(2))
                .build();
        final List<OAuthPublicKey> validPublicKeys = Arrays.asList(
                publicKeyOne,
                publicKeyTwo
        );

        // when
        inMemoryRepository.refreshPublicKeys(validPublicKeys);

        // then
        assertThat(inMemoryRepository.retrieveActivePublicKeys(), is(validPublicKeys));
    }

    @Test
    public void shouldThrowExceptionWhenInactiveKeysWhereGiven() {
        // given
        final ZonedDateTime now = ZonedDateTime.now();
        final OAuthPublicKey publicKeyOne = oAuthPublicKeyBuilder()
                .withPublicKey("publicKeyOne")
                .withPublicKeyFingerprint("fingerPrintOne")
                .withValidFrom(now.minusDays(2))
                .withValidUntil(now.minusDays(1))
                .build();
        final List<OAuthPublicKey> invalidPublicKeys = Collections.singletonList(publicKeyOne);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> inMemoryRepository.refreshPublicKeys(invalidPublicKeys), "Invalid public keys retrieved");
    }
}