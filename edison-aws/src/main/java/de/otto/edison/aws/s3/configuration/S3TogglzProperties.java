package de.otto.edison.aws.s3.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Validated
@ConfigurationProperties(prefix = "edison.aws.s3.togglz")
public class S3TogglzProperties {
    private boolean enabled = true;
    private String bucketName;
    private String keyPrefix = "togglz/";
    private boolean prefetch = false;

    /**
     * Number of millis used to cache toggle state. cache-ttl=0 will disable caching.
     */
    @Min(0)
    private int cacheTtl = 30000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(final String bucketName) {
        this.bucketName = bucketName;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(final String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public boolean isPrefetch() {
        return prefetch;
    }

    public void setPrefetch(final boolean prefetch) {
        this.prefetch = prefetch;
    }

    public int getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(final int cacheTtl) {
        this.cacheTtl = cacheTtl;
    }
}
