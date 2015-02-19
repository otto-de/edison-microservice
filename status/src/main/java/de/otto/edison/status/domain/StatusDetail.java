package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.util.LinkedHashMap;
import java.util.Map;

import static de.otto.edison.status.domain.Status.*;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

@Immutable
public class StatusDetail {

    private final String name;
    private final Status status;
    private final String message;
    private final Map<String, String> details;

    private StatusDetail(final String name,
                         final Status status,
                         final String message,
                         final Map<String, String> details) {
        this.name = name;
        this.status = status;
        this.message = message;
        this.details = unmodifiableMap(new LinkedHashMap<>(details));
    }

    public static StatusDetail statusDetail(final String name,
                                            final Status status,
                                            final String message) {
        return new StatusDetail(name, status, message, emptyMap());
    }

    public static StatusDetail statusDetail(final String name,
                                            final Status status,
                                            final String message,
                                            final Map<String, String> additionalAttributes) {
        return new StatusDetail(name, status, message, additionalAttributes);
    }

    public StatusDetail toOk(final String message) {
        return statusDetail(name, OK, message, details);
    }

    public StatusDetail toWarning(final String message) {
        return statusDetail(name, WARNING, message, details);
    }

    public StatusDetail toError(final String message) {
        return statusDetail(name, ERROR, message, details);
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatusDetail that = (StatusDetail) o;

        if (details != null ? !details.equals(that.details) : that.details != null) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (status != that.status) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StatusDetail{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", details=" + details +
                '}';
    }
}
