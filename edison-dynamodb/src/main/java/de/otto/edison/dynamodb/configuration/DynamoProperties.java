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
    private String accessKey = "test1";
    private String secretKey = "test2";

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(final String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final String endpoint) {
        this.endpoint = endpoint;
    }
}
