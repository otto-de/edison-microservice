package de.otto.µservice.jobs.domain;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.LocalDateTime.now;

public final class JobInfo {

    private final URI jobUri;
    private final JobType jobType;
    private ExecutionState state;
    private JobStatus status;
    private LocalDateTime started;
    private LocalDateTime stopped;

    public enum JobStatus { OK, ERROR}

    public enum ExecutionState { RUNNING, STOPPED}

    public JobInfo(final JobType type, final URI jobUri) {
        this.jobUri = jobUri;
        this.jobType = type;
        state = ExecutionState.RUNNING;
        status = JobStatus.OK;
        started = now();
    }

    public URI getJobUri() {
        return jobUri;
    }

    public JobType getJobType() {
        return jobType;
    }

    public ExecutionState getState() {
        return state;
    }

    public void setState(final ExecutionState state) {
        this.state = state;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(final JobStatus status) {
        this.status = status;
    }

    public LocalDateTime getStarted() {
        return started;
    }

    public void setStarted(final LocalDateTime started) {
        this.started = started;
    }

    public Optional<LocalDateTime> getStopped() {
        return Optional.ofNullable(stopped);
    }

    public void setStopped(final LocalDateTime stopped) {
        this.stopped = stopped;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobInfo job = (JobInfo) o;

        if (jobType != null ? !jobType.equals(job.jobType) : job.jobType != null) return false;
        if (jobUri != null ? !jobUri.equals(job.jobUri) : job.jobUri != null) return false;
        if (started != null ? !started.equals(job.started) : job.started != null) return false;
        if (state != job.state) return false;
        if (status != job.status) return false;
        if (stopped != null ? !stopped.equals(job.stopped) : job.stopped != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jobUri != null ? jobUri.hashCode() : 0;
        result = 31 * result + (jobType != null ? jobType.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (started != null ? started.hashCode() : 0);
        result = 31 * result + (stopped != null ? stopped.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Job{" +
                "jobUri=" + jobUri +
                ", jobType=" + jobType +
                ", state=" + state +
                ", status=" + status +
                ", started=" + started +
                ", stopped=" + stopped +
                '}';
    }
}
