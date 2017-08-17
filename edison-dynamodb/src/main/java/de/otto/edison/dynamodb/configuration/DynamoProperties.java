package de.otto.edison.dynamodb.configuration;

import de.otto.edison.annotations.Beta;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties used to configure DynamoDB clients.
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "edison.dynamo")
@Validated
@Beta
public class DynamoProperties {

    private String endpoint = "http://localhost:8000/";
    private String profileName = "test";

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(final String profileName) {
        this.profileName = profileName;
    }
}
