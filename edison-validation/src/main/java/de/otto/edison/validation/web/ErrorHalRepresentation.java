package de.otto.edison.validation.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.otto.edison.hal.HalRepresentation;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static de.otto.edison.hal.Link.profile;
import static de.otto.edison.hal.Links.linkingTo;

@JsonDeserialize(builder = ErrorHalRepresentation.Builder.class)
public class ErrorHalRepresentation extends HalRepresentation {

    private final String errorMessage;
    private final Map<String, List<Map<String, String>>> errors;

    private ErrorHalRepresentation(Builder builder) {
        super(linkingTo()
                .array(profile(builder.profile))
                .build()
        );
        this.errors = builder.errors;
        this.errorMessage = builder.errorMessage;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "ErrorHalRepresentation{" +
                "withErrorMessage='" + errorMessage + '\'' +
                ", withErrors=" + errors +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ErrorHalRepresentation that = (ErrorHalRepresentation) o;
        return Objects.equals(errors, that.errors) &&
                Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), errors, errorMessage);
    }

    public Map<String, List<Map<String, String>>> getErrors() {

        return errors;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(NON_NULL)
    public static final class Builder {
        private Map<String, List<Map<String, String>>> errors = new HashMap<>();
        private String errorMessage;
        private String profile = "";

        private Builder() {
        }

        public ErrorHalRepresentation build() {
            return new ErrorHalRepresentation(this);
        }

        public Builder withProfile(String profile) {
            this.profile = profile;
            return this;
        }

        public Builder withErrors(Map<String, List<Map<String, String>>> errors) {
            this.errors = errors;
            return this;
        }

        public Builder withError(String field, String key, String message, String rejected) {
            Map<String, String> innerMap = new HashMap<>();
            innerMap.put("key", key);
            innerMap.put("message", message);
            innerMap.put("rejected", rejected);
            List<Map<String, String>> list = errors.getOrDefault(field, new ArrayList<>());
            list.add(innerMap);
            errors.put(field, list);
            return this;
        }

        public Builder withErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }


    }
}
