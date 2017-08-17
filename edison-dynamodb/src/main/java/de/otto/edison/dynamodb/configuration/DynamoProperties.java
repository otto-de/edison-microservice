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
    private String tableNamePrefix = "test";
    private String tableNameSeparator = "-";

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

    public String getTableNamePrefix() {
        return tableNamePrefix;
    }

    public void setTableNamePrefix(final String tableNamePrefix) {
        this.tableNamePrefix = tableNamePrefix;
    }

    public String getTableNameSeparator() {
        return tableNameSeparator;
    }

    public void setTableNameSeparator(final String tableNameSeparator) {
        this.tableNameSeparator = tableNameSeparator;
    }
}
