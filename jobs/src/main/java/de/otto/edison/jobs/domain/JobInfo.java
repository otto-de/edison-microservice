package de.otto.edison.jobs.domain;

import net.jcip.annotations.ThreadSafe;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;

@ThreadSafe
public final class JobInfo {

    private final URI jobUri;
    private final JobType jobType;
    private final OffsetDateTime started;
    private final Optional<OffsetDateTime> stopped;
    private final List<JobMessage> messages;
    private final JobStatus status;
    private final OffsetDateTime lastUpdated;

    public enum JobStatus { OK, ERROR, DEAD;}


    JobInfo(final JobType type,
            final URI jobUri,
            final OffsetDateTime started,
            final Optional<OffsetDateTime> stopped,
            final List<JobMessage> messages,
            final JobStatus status, OffsetDateTime lastUpdated) {
        this.jobUri = jobUri;
        this.jobType = type;
        this.started = started;
        this.stopped = stopped;
        this.lastUpdated = lastUpdated;
        this.messages = unmodifiableList(new ArrayList<>(messages));
        this.status = status;
    }

    public URI getJobUri() {
        return jobUri;
    }

    public JobType getJobType() {
        return jobType;
    }

    public JobStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStarted() {
        return started;
    }

    public String getState() {
        return stopped.isPresent() ? "STOPPED" : "RUNNING";
    }

    public Optional<OffsetDateTime> getStopped() {
        return stopped;
    }

    public List<JobMessage> getMessages() {
        return messages;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
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
