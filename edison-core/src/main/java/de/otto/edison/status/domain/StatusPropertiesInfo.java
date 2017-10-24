package de.otto.edison.status.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.otto.edison.annotations.Beta;
import de.otto.edison.status.configuration.StatusPropertiesInfoProperties;
import net.jcip.annotations.Immutable;

import java.util.Map;
import java.util.Set;

@Beta
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusPropertiesInfo {

    private Map<String, String> properties;

    public StatusPropertiesInfo() {
    }

    public StatusPropertiesInfo(final Map<String, String> properties) {
        this.properties = properties;
    }

    public static StatusPropertiesInfo extendedInfo(final StatusPropertiesInfoProperties statusPropertiesInfoProperties) {
        return new StatusPropertiesInfo(statusPropertiesInfoProperties.getProperties());
    }

    public String get(final String property) {
        return properties.get(property);
    }

    public String getOrDefault(final String property, final String defaultValue) {
        return properties.getOrDefault(property, defaultValue);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return properties.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatusPropertiesInfo)) return false;

        StatusPropertiesInfo that = (StatusPropertiesInfo) o;

        return properties.equals(that.properties);
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatusPropertiesInfo{");
        sb.append("properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }
}
