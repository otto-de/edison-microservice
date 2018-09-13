package de.otto.edison.oauth;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Objects;

@Validated
@JsonDeserialize(builder = OAuthPublicKey.Builder.class)
public class OAuthPublicKey {

    private final String publicKey;
    private final String publicKeyFingerprint;
    private final ZonedDateTime validFrom;
    private final ZonedDateTime validUntil;

    private OAuthPublicKey(final Builder builder) {
        this.publicKey = builder.publicKey;
        this.publicKeyFingerprint = builder.publicKeyFingerprint;
        this.validFrom = builder.validFrom;
        this.validUntil = builder.validUntil;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPublicKeyFingerprint() {
        return publicKeyFingerprint;
    }

    public ZonedDateTime getValidFrom() {
        return validFrom;
    }

    public ZonedDateTime getValidUntil() {
        return validUntil;
    }

    public static Builder oAuthPublicKeyBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final OAuthPublicKey that = (OAuthPublicKey) o;
        return Objects.equals(publicKey, that.publicKey) &&
                Objects.equals(publicKeyFingerprint, that.publicKeyFingerprint) &&
                Objects.equals(validFrom, that.validFrom) &&
                Objects.equals(validUntil, that.validUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicKey, publicKeyFingerprint, validFrom, validUntil);
    }

    @Override
    public String toString() {
        return "OAuthPublicKey{" +
                "publicKey='" + publicKey + '\'' +
                ", publicKeyFingerprint='" + publicKeyFingerprint + '\'' +
                ", validFrom=" + validFrom +
                ", validUntil=" + validUntil +
                '}';
    }

    static class Builder {
        @NotNull
        private String publicKey;

        @NotNull
        private String publicKeyFingerprint;

        private ZonedDateTime validFrom;

        private ZonedDateTime validUntil;

        public Builder withPublicKey(final String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder withPublicKeyFingerprint(final String publicKeyFingerprint) {
            this.publicKeyFingerprint = publicKeyFingerprint;
            return this;
        }

        public Builder withValidFrom(final ZonedDateTime validFrom) {
            this.validFrom = validFrom;
            return this;
        }

        public Builder withValidUntil(final ZonedDateTime validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public OAuthPublicKey build() {
            return new OAuthPublicKey(this);
        }
    }
}
