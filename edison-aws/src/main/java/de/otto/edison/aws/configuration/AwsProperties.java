package de.otto.edison.aws.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static software.amazon.awssdk.regions.Region.EU_CENTRAL_1;


@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    private String region = EU_CENTRAL_1.id();
    private String profile = "default";

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(final String profile) {
        this.profile = profile;
    }

    @Override
    public String toString() {
        return "AwsProperties{" +
                "region='" + region + '\'' +
                ", profile='" + profile + '\'' +
                '}';
    }
}
