package de.otto.edison.jobs.domain;

import de.otto.edison.jobs.monitor.JobMonitor;
import net.jcip.annotations.ThreadSafe;

import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.*;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.domain.Level.WARNING;
import static java.time.OffsetDateTime.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@ThreadSafe
public class JobInfo {
    private static final String JOB_DEAD_MESSAGE = "Job didn't receive updates for a while, considering it dead";

    private final JobMonitor monitor;
    private final Clock clock;
    private final URI jobUri;
    private final String jobType;
    private final OffsetDateTime started;
    private final List<JobMessage> messages = new ArrayList<>();
    private Optional<OffsetDateTime> stopped;
    private JobStatus status;
    private OffsetDateTime lastUpdated;

    public enum JobStatus { OK, ERROR, DEAD;}

    public static JobInfo newJobInfo(final String jobType,
                                     final URI jobUri,
                                     final JobMonitor monitor,
                                     final Clock clock) {
        return new JobInfo(jobType, jobUri, monitor, clock);
    }

    JobInfo(final String jobType,
            final URI jobUri,
            final JobMonitor monitor,
            final Clock clock) {
        this.clock = clock;
        this.jobUri = jobUri;
        this.jobType = jobType;
        this.started = now(clock);
        this.stopped = empty();
        this.status = OK;
        this.monitor = monitor;
        this.lastUpdated = started;
        this.messages.add(jobMessage(Level.INFO, "Started " + jobType));
        this.monitor.update(this);
    }

    public synchronized boolean isStopped() {
        return stopped.isPresent();
    }

    public URI getJobUri() {
        return jobUri;
    }

    public String getJobType() {
        return jobType;
    }

    public OffsetDateTime getStarted() {
        return started;
    }

    public synchronized JobStatus getStatus() {
        return status;
    }

    public synchronized String getState() {
        return isStopped() ? "STOPPED" : "RUNNING";
    }

    public synchronized Optional<OffsetDateTime> getStopped() {
        return stopped;
    }

    public synchronized List<JobMessage> getMessages() {
        return messages;
    }

    public synchronized OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }


    public synchronized void ping() {
        lastUpdated = now(clock);
        monitor.update(this);
    }

    public synchronized JobInfo info(final String message) {
        messages.add(jobMessage(Level.INFO, message));
        lastUpdated = now(clock);
        monitor.update(this);
        return this;
    }

    public synchronized JobInfo error(final String message) {
        messages.add(jobMessage(Level.ERROR, message));
        lastUpdated = now(clock);
        status = ERROR;
        monitor.update(this);
        return this;
    }

    public synchronized JobInfo stop() {
        lastUpdated = now(clock);
        stopped = of(lastUpdated);
        monitor.update(this);
        return this;
    }

    public synchronized JobInfo dead() {
        messages.add(jobMessage(WARNING, JOB_DEAD_MESSAGE));
        lastUpdated = now(clock);
        stopped = of(lastUpdated);
        status = DEAD;
        monitor.update(this);
        return this;
    }

    JobMonitor getMonitor() {
        return monitor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobInfo jobInfo = (JobInfo) o;

        if (jobType != null ? !jobType.equals(jobInfo.jobType) : jobInfo.jobType != null) return false;
        if (jobUri != null ? !jobUri.equals(jobInfo.jobUri) : jobInfo.jobUri != null) return false;
        if (lastUpdated != null ? !lastUpdated.equals(jobInfo.lastUpdated) : jobInfo.lastUpdated != null) return false;
        if (messages != null ? !messages.equals(jobInfo.messages) : jobInfo.messages != null) return false;
        if (started != null ? !started.equals(jobInfo.started) : jobInfo.started != null) return false;
        if (status != jobInfo.status) return false;
        if (stopped != null ? !stopped.equals(jobInfo.stopped) : jobInfo.stopped != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jobUri != null ? jobUri.hashCode() : 0;
        result = 31 * result + (jobType != null ? jobType.hashCode() : 0);
        result = 31 * result + (started != null ? started.hashCode() : 0);
        result = 31 * result + (stopped != null ? stopped.hashCode() : 0);
        result = 31 * result + (messages != null ? messages.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JobInfo{" +
                "jobUri=" + jobUri +
                ", jobType=" + jobType +
                ", started=" + started +
                ", stopped=" + stopped +
                ", messages=" + messages +
                ", status=" + status +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
