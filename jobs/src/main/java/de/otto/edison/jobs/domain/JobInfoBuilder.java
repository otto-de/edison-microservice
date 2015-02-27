package de.otto.edison.jobs.domain;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.time.LocalDateTime.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class JobInfoBuilder {
    private final JobType type;
    private final URI jobUri;
    private final List<JobMessage> messages;
    private LocalDateTime started;
    private Optional<LocalDateTime> stopped;
    private JobInfo.ExecutionState state;
    private JobInfo.JobStatus status;

    private JobInfoBuilder(final JobType type, final URI jobUri) {
        this.type = type;
        this.jobUri = jobUri;
        messages = new CopyOnWriteArrayList<>();
        state = JobInfo.ExecutionState.RUNNING;
        status = JobInfo.JobStatus.OK;
        started = now();
        stopped = empty();
    }

    public JobInfoBuilder(final JobInfo prototype) {
        this.type = prototype.getJobType();
        this.jobUri = prototype.getJobUri();
        this.started = prototype.getStarted();
        this.stopped = prototype.getStopped();
        this.messages = new CopyOnWriteArrayList<>(prototype.getMessages());
        this.state = prototype.getState();
        this.status = prototype.getStatus();
    }

    public static JobInfoBuilder jobInfoBuilder(final JobType type, final URI uri) {
        return new JobInfoBuilder(type, uri);
    }

    public static JobInfoBuilder copyOf(final JobInfo prototype) {
        return new JobInfoBuilder(prototype);
    }

    public JobInfoBuilder addMessage(final JobMessage message) {
        this.messages.add(message);
        return this;
    }

    public JobInfoBuilder withStarted(final LocalDateTime started) {
        this.started = started;
        return this;
    }

    public JobInfoBuilder withState(final JobInfo.ExecutionState state) {
        this.state = state;
        return this;
    }

    public JobInfoBuilder withStatus(final JobInfo.JobStatus status) {
        this.status = status;
        return this;
    }

    public JobInfoBuilder withStopped(final LocalDateTime stopped) {
        this.stopped = of(stopped);
        return this;
    }

    public JobInfo build() {
        return new JobInfo(type, jobUri, started, stopped, messages, state, status);
    }
}