package de.otto.edison.aws.s3.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edison.aws.config.s3")
public class S3Properties {

    private String bucketname;
    private String filename;
    private boolean enabled;

    public String getBucketname() {
        return bucketname;
    }

    public void setBucketname(final String bucketname) {
        this.bucketname = bucketname;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "S3Properties{" +
                "bucketname='" + bucketname + '\'' +
                ", filename='" + filename + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
