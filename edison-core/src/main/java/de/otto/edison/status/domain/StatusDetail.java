package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.time.Instant;
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
    private final Instant since;

    private StatusDetail(final String name,
                         final Status status,
                         final String message,
                         final List<Link> links,
                         final Map<String, String> details) {
        this(name, status, message, links, details, null);
    }

    private StatusDetail(final String name,
                         final Status status,
                         final String message,
                         final List<Link> links,
                         final Map<String, String> details,
                         final Instant since) {
        this.name = name;
        this.status = status;
        this.message = message;
        this.links = unmodifiableList(links);
        this.details = unmodifiableMap(new LinkedHashMap<>(details));
        this.since = since;
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
        return copyWith(OK, message, details);
    }

    /**
     * Create a copy of this StatusDetail, updates the message, changes the status to WARNING,
     * and return the new StatusDetail.
     *
     * @param message the new message
     * @return StatusDetail
     */
    public StatusDetail toWarning(final String message) {
        return copyWith(WARNING, message, details);
    }

    /**
     * Create a copy of this StatusDetail, updates the message, changes the status to ERROR,
     * and return the new StatusDetail.
     *
     * @param message the new message
     * @return StatusDetail
     */
    public StatusDetail toError(final String message) {
        return copyWith(ERROR, message, details);
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
        return copyWith(status, message, newDetails);
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
        return copyWith(status, message, newDetails);
    }

    /**
     * Create a copy of this StatusDetail with the given {@link #getSince() since} timestamp.
     *
     * This is used by {@link de.otto.edison.status.indicator.StatusDetailSinceCache} to keep track of
     * the first occurrence of the current {@link Status}. There is normally no need to call this method
     * from a {@link de.otto.edison.status.indicator.StatusDetailIndicator}.
     *
     * @param since the timestamp of the first occurrence of the current status, may be null
     * @return StatusDetail
     */
    public StatusDetail withSince(final Instant since) {
        return new StatusDetail(name, status, message, links, details, since);
    }

    /**
     * Creates a copy of this StatusDetail. The {@link #getSince() since} timestamp is only retained if the
     * status did not change, because it marks the first occurrence of the current status.
     */
    private StatusDetail copyWith(final Status newStatus,
                                  final String newMessage,
                                  final Map<String, String> newDetails) {
        return new StatusDetail(name, newStatus, newMessage, emptyList(), newDetails,
                newStatus == status ? since : null);
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

    /**
     * The timestamp of the first occurrence of the current {@link #getStatus() status}.
     *
     * The value only changes when the status changes: as long as a StatusDetail keeps reporting the same
     * Status, this timestamp stays the same, even though StatusDetails are recreated on every update.
     * The timestamp is maintained by {@link de.otto.edison.status.indicator.StatusDetailSinceCache} while
     * the {@link de.otto.edison.status.indicator.ApplicationStatusAggregator} aggregates the application
     * status, so StatusDetails that were not aggregated yet return null here.
     *
     * @return the timestamp of the first occurrence of the current status, or null if unknown
     */
    public Instant getSince() {
        return since;
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
        if (details != null ? !details.equals(detail.details) : detail.details != null) return false;
        return since != null ? since.equals(detail.since) : detail.since == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (links != null ? links.hashCode() : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        result = 31 * result + (since != null ? since.hashCode() : 0);
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
                ", since=" + since +
                '}';
    }

}
