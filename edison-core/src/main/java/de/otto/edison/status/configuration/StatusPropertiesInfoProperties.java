package de.otto.edison.status.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "edison.status")
public class StatusPropertiesInfoProperties {

    private Map<String, String> properties = new HashMap<>();

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
