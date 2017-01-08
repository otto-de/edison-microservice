package de.otto.edison.togglz.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class FeatureToggleRepresentation {

    public final String description;
    public final boolean enabled;
    public final String value;


    FeatureToggleRepresentation(final String description, final boolean enabled, final String value) {
        this.description = description;
        this.enabled = enabled;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeatureToggleRepresentation that = (FeatureToggleRepresentation) o;

        if (enabled != that.enabled) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;

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
}
