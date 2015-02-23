package de.otto.edison.jobs.domain;

import net.jcip.annotations.ThreadSafe;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.time.LocalDateTime.now;

@ThreadSafe
public final class JobInfo {

    private final URI jobUri;
    private final JobType jobType;
    private volatile LocalDateTime started;
    private volatile LocalDateTime stopped;
    private volatile List<JobMessage> messages = new CopyOnWriteArrayList<>();
    private volatile ExecutionState state;
    private volatile JobStatus status;

    public enum JobStatus { OK, ERROR;}


    public enum ExecutionState { RUNNING, STOPPED;}

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

    public void addMessage(final JobMessage message) {
        this.messages.add(message);
    }

    public List<JobMessage> getMessages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobInfo jobInfo = (JobInfo) o;

        if (jobType != null ? !jobType.equals(jobInfo.jobType) : jobInfo.jobType != null) return false;
        if (jobUri != null ? !jobUri.equals(jobInfo.jobUri) : jobInfo.jobUri != null) return false;
        if (messages != null ? !messages.equals(jobInfo.messages) : jobInfo.messages != null) return false;
        if (started != null ? !started.equals(jobInfo.started) : jobInfo.started != null) return false;
        if (state != jobInfo.state) return false;
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
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JobInfo{" +
                "jobType=" + jobType +
                ", jobUri=" + jobUri +
                ", started=" + started +
                ", stopped=" + stopped +
                ", messages=" + messages +
                ", state=" + state +
                ", status=" + status +
                '}';
    }
}
