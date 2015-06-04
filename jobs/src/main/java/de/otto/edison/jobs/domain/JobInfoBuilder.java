package de.otto.edison.jobs.domain;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.time.OffsetDateTime.now;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class JobInfoBuilder {
    private final String type;
    private final URI jobUri;
    private final List<JobMessage> messages;
    private OffsetDateTime started;
    private Optional<OffsetDateTime> stopped;
    private JobInfo.JobStatus status;
    private OffsetDateTime lastUpdated;

    private JobInfoBuilder(final String type, final URI jobUri) {
        this.type = type;
        this.jobUri = jobUri;
        messages = new CopyOnWriteArrayList<>();
        status = JobInfo.JobStatus.OK;
        started = now();
        stopped = empty();
        lastUpdated = OffsetDateTime.now();
    }

    private JobInfoBuilder(final JobInfo prototype) {
        this.type = prototype.getJobType();
        this.jobUri = prototype.getJobUri();
        this.started = prototype.getStarted();
        this.stopped = prototype.getStopped();
        this.messages = new CopyOnWriteArrayList<>(prototype.getMessages());
        this.status = prototype.getStatus();
        this.lastUpdated = prototype.getLastUpdated();
    }

    public static JobInfoBuilder jobInfoBuilder(final String type, final URI uri) {
        return new JobInfoBuilder(type, uri);
    }

    public static JobInfoBuilder copyOf(final JobInfo prototype) {
        return new JobInfoBuilder(prototype);
    }

    public JobInfoBuilder addMessage(final JobMessage message) {
        this.messages.add(message);
        return this;
    }

    public JobInfoBuilder addMessages(final List<JobMessage> messages) {
        this.messages.addAll(messages);
        return this;
    }

    public JobInfoBuilder withStarted(final OffsetDateTime started) {
        this.started = started;
        return this;
    }

    public JobInfoBuilder withStatus(final JobInfo.JobStatus status) {
        this.status = status;
        return this;
    }

    public JobInfoBuilder withStopped(final OffsetDateTime stopped) {
        this.stopped = ofNullable(stopped);
        return this;
    }

    public JobInfoBuilder withLastUpdated(final OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public JobInfo build() {
        return new JobInfo(type, jobUri, started, stopped, messages, status, lastUpdated);
    }
}