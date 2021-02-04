package de.otto.edison.togglz.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;

/**
 * Configuration properties used to configure edison-togglz
 */
@ConfigurationProperties(prefix = "edison.togglz")
@Validated
public class TogglzProperties {

    /**
     * Number of millis used to cache toggle state. cache-ttl=0 will disable caching.
     */
    @Min(0)
    private int cacheTtl = 60000;
    /**
     * Enable / Disable the Togglz web console.
     */
    @Valid
    private Console console = new Console();

    @Valid
    private S3 s3 = new S3();

    @Valid
    private Mongo mongo = new Mongo();

    public int getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(final int cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public Console getConsole() {
        return console;
    }

    public void setConsole(final Console console) {
        this.console = console;
    }

    public S3 getS3() {
        return s3;
    }

    public void setS3(S3 s3) {
        this.s3 = s3;
    }

    public Mongo getMongo() {
        return mongo;
    }

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    public static class Console {
        /**
         * Enable / disable the Togglz web console.
         */
        private boolean enabled = true;
        /**
         * Validate CSRF Token before update of toggle.
         */
        private boolean validateCSRFToken = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isValidateCSRFToken() {
            return validateCSRFToken;
        }

        public void setValidateCSRFToken(boolean validateCSRFToken) {
            this.validateCSRFToken = validateCSRFToken;
        }
    }

    public static class S3 {
        /**
         * Enable / disable the S3 togglz.
         */
        private boolean enabled = false;
        private String bucketName;
        private String keyPrefix = "togglz/";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
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
    }

    public static class Mongo {
        /**
         * Enable / disable the mongo togglz.
         */
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }

}
