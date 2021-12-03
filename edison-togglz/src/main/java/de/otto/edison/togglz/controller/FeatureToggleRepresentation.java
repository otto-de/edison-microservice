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
    public final String strategy;
    public final String value;
    public final List<String> groups;

    private FeatureToggleRepresentation(Builder builder) {
        description = builder.description;
        enabled = builder.enabled;
        strategy = builder.strategy;
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
        return enabled == that.enabled && Objects.equals(description, that.description) && Objects.equals(strategy, that.strategy) && Objects.equals(value, that.value) && Objects.equals(groups, that.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, enabled, strategy, value, groups);
    }

    @Override
    public String toString() {
        return "FeatureToggleRepresentation{" +
                "description='" + description + '\'' +
                ", enabled=" + enabled +
                ", strategy='" + strategy + '\'' +
                ", value='" + value + '\'' +
                ", groups=" + groups +
                '}';
    }

    public static final class Builder {
        private String description;
        private boolean enabled;
        private String strategy;
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

        public Builder withStrategy(String val) {
            strategy = val;
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
