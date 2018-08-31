package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.otto.edison.status.domain.Status.ERROR;
import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * The status of a job, component, repository or other part of the application.
 *
 * StatusDetails are {@link de.otto.edison.status.indicator.ApplicationStatusAggregator aggreagated}
 * to the overall status of the application. This information is exposed by the
 * {@link de.otto.edison.status.controller.StatusController} via REST.
 *
 * In order to accounce the current status of some part of the application,
 * {@link de.otto.edison.status.indicator.StatusDetailIndicator} can be implemented.
 */
@Immutable
public class StatusDetail {

    private final String name;
    private final Status status;
    private final String message;
    private final List<Link> links;
    private final Map<String, String> details;

    private StatusDetail(final String name,
                         final Status status,
                         final String message,
                         final List<Link> links,
                         final Map<String, String> details) {
        this.name = name;
        this.status = status;
        this.message = message;
        this.links = unmodifiableList(links);
        this.details = unmodifiableMap(new LinkedHashMap<>(details));
    }

    public static StatusDetail statusDetail(final String name,
                                            final Status status,
                                            final String message) {
        return new StatusDetail(name, status, message, emptyList(), emptyMap());
    }

    public static StatusDetail statusDetail(final String name,
                                            final Status status,
                                            final String message,
                                            final Map<String, String> additionalAttributes) {
        return new StatusDetail(name, status, message, emptyList(), additionalAttributes);
    }

    public static StatusDetail statusDetail(final String name,
                                            final Status status,
                                            final String message,
                                            final Link link) {
        return new StatusDetail(name, status, message, singletonList(link), emptyMap());
    }

    public static StatusDetail statusDetail(final String name,
                                            final Status status,
                                            final String message,
                                            final List<Link> links) {
        return new StatusDetail(name, status, message, links, emptyMap());
    }

    public static StatusDetail statusDetail(final String name,
                                            final Status status,
                                            final String message,
                                            final Link link,
                                            final Map<String, String> additionalAttributes) {
        return new StatusDetail(name, status, message, singletonList(link), additionalAttributes);
    }

    public static StatusDetail statusDetail(final String name,
                                            final Status status,
                                            final String message,
                                            final List<Link> links,
                                            final Map<String, String> additionalAttributes) {
        return new StatusDetail(name, status, message, links, additionalAttributes);
    }

    /**
     * Create a copy of this StatusDetail, updates the message, changes the status to OK,
     * and return the new StatusDetail.
     *
     * @param message the new message
     * @return StatusDetail
     */
    public StatusDetail toOk(final String message) {
        return statusDetail(name, OK, message, details);
    }

    /**
     * Create a copy of this StatusDetail, updates the message, changes the status to WARNING,
     * and return the new StatusDetail.
     *
     * @param message the new message
     * @return StatusDetail
     */
    public StatusDetail toWarning(final String message) {
        return statusDetail(name, WARNING, message, details);
    }

    /**
     * Create a copy of this StatusDetail, updates the message, changes the status to ERROR,
     * and return the new StatusDetail.
     *
     * @param message the new message
     * @return StatusDetail
     */
    public StatusDetail toError(final String message) {
        return statusDetail(name, ERROR, message, details);
    }

    /**
     * Create a copy of this StatusDetail, add a detail and return the new StatusDetail.
     *
     * @param key the key of the additional detail
     * @param value the value of the additional detail
     * @return StatusDetail
     */
    public StatusDetail withDetail(final String key, final String value) {
        final LinkedHashMap<String, String> newDetails = new LinkedHashMap<>(details);
        newDetails.put(key, value);
        return statusDetail(name,status,message, newDetails);
    }

    /**
     * Create a copy of this StatusDetail, remove a detail and return the new StatusDetail.
     *
     * @param key the key of the additional detail
     * @return StatusDetail
     */
    public StatusDetail withoutDetail(final String key) {
        final LinkedHashMap<String, String> newDetails = new LinkedHashMap<>(details);
        newDetails.remove(key);
        return statusDetail(name,status,message, newDetails);
    }

    /**
     * Short name of the status detail.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Status indicating the health / availability of the job, component, ...
     *
     * @return Status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * A short message describing the current status.
     *
     * @return String
     */
    public String getMessage() {
        return message;
    }

    public List<Link> getLinks() {
        return links;
    }

    /**
     * Additional details about the current status of the job, component, ...
     *
     * @return Map
     */
    public Map<String, String> getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatusDetail detail = (StatusDetail) o;

        if (name != null ? !name.equals(detail.name) : detail.name != null) return false;
        if (status != detail.status) return false;
        if (message != null ? !message.equals(detail.message) : detail.message != null) return false;
        if (links != null ? !links.equals(detail.links) : detail.links != null) return false;
        return details != null ? details.equals(detail.details) : detail.details == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (links != null ? links.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StatusDetail{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", links=" + links +
                ", details=" + details +
                '}';
    }

}
