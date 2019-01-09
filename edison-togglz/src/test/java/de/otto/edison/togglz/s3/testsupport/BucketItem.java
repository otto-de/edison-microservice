package de.otto.edison.togglz.s3.testsupport;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public class BucketItem {

    private final String name;
    private final byte[] data;

    private final Instant lastModified;

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    private BucketItem(Builder builder) {
        name = builder.name;
        data = builder.data;
        lastModified = builder.lastModified;
    }

    public static Builder bucketItemBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BucketItem that = (BucketItem) o;
        return Objects.equals(name, that.name) &&
                Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, data);
    }

    public static Builder bucketItemBuilder(BucketItem copy) {
        Builder builder = new Builder();
        builder.name = copy.getName();
        builder.data = copy.getData();
        builder.lastModified = copy.lastModified;
        return builder;
    }

    public static final class Builder {
        private String name;
        private byte[] data;
        private Instant lastModified;

        private Builder() {
        }

        public Builder withName(String val) {
            name = val;
            return this;
        }

        public Builder withData(byte[] val) {
            data = val;
            return this;
        }

        public Builder withLastModified(Instant val) {
            lastModified = val;
            return this;
        }

        public Builder withLastModifiedNow() {
            return withLastModified(Instant.now());
        }

        public BucketItem build() {
            return new BucketItem(this);
        }
    }
}
