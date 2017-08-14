package de.otto.edison.dynamodb.configuration;

import de.otto.edison.annotations.Beta;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Properties used to configure MongoDB clients.
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "edison.dynamo")
@Validated
@Beta
public class DynamoProperties {

    private String endpoint = "http://localhost:8000/";
    private String accessKeyId = "test1";
    private String secretKeyId = "test2";

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(final String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretKeyId() {
        return secretKeyId;
    }

    public void setSecretKeyId(final String secretKeyId) {
        this.secretKeyId = secretKeyId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }
}
