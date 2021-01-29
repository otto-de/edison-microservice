package de.otto.edison.togglz.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class FeatureToggleRepresentation {

    public final String description;
    public final boolean enabled;
    public final String value;
    public final List<String> groups;

    private FeatureToggleRepresentation(Builder builder) {
        description = builder.description;
        enabled = builder.enabled;
        value = builder.value;
        groups = builder.groups;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeatureToggleRepresentation that = (FeatureToggleRepresentation) o;

        if (enabled != that.enabled) return false;
        if (!Objects.equals(description, that.description)) return false;
        return Objects.equals(value, that.value);

    }

    @Override
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FeatureToggleRepresentation{" +
                "description='" + description + '\'' +
                ", enabled=" + enabled +
                ", value='" + value + '\'' +
                '}';
    }

    public static final class Builder {
        private String description;
        private boolean enabled;
        private String value;
        private List<String> groups = new ArrayList<>();

        private Builder() {
        }

        public Builder withDescription(String val) {
            description = val;
            return this;
        }

        public Builder withEnabled(boolean val) {
            enabled = val;
            return this;
        }

        public Builder withValue(String val) {
            value = val;
            return this;
        }

        public Builder withGroups(List<String> val) {
            groups = val;
            return this;
        }

        public FeatureToggleRepresentation build() {
            return new FeatureToggleRepresentation(this);
        }
    }
}
