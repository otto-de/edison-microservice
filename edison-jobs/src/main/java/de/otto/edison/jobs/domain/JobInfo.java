package de.otto.edison.jobs.domain;

import net.jcip.annotations.ThreadSafe;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Information about a single job execution.
 * <p>
 * A JobInfo instance is created for every job execution. It is constantly updated by the background job and
 * persisted in the JobRepository.
 */
@ThreadSafe
public class JobInfo {
    private final String jobId;
    private final String jobType;
    private final OffsetDateTime started;
    private final List<JobMessage> messages;
    private final Optional<OffsetDateTime> stopped;
    private final JobStatus status;
    private final OffsetDateTime lastUpdated;
    private final String hostname;
    private final Clock clock;

    public Clock getClock() {
        return clock;
    }

    public enum JobStatus {OK, SKIPPED, ERROR, DEAD}

    public static JobInfo newJobInfo(final String jobId, final String jobType,
                                     final Clock clock, final String hostname) {
        return new JobInfo(jobType, jobId, clock, hostname);
    }

    public static JobInfo newJobInfo(final String jobId,
                                     final String jobType,
                                     final OffsetDateTime started,
                                     final OffsetDateTime lastUpdated,
                                     final Optional<OffsetDateTime> stopped,
                                     final JobStatus status,
                                     final List<JobMessage> messages,
                                     final Clock clock,
                                     final String hostname) {
        return new JobInfo(jobId, jobType, started, lastUpdated, stopped, status, messages, clock, hostname);
    }

    private JobInfo(final String jobType, final String jobId, final Clock clock, final String hostname) {
        this.jobId = jobId;
        this.jobType = jobType;
        //Truncate to milliseconds precision because current persistence implementations only support milliseconds
        this.started = now(clock).truncatedTo(ChronoUnit.MILLIS);
        this.clock = clock;
        this.stopped = empty();
        this.status = OK;
        this.lastUpdated = started;
        this.hostname = hostname;
        this.messages = emptyList();
    }

    private JobInfo(final String jobId,
                    final String jobType,
                    final OffsetDateTime started,
                    final OffsetDateTime lastUpdated,
                    final Optional<OffsetDateTime> stopped,
                    final JobStatus status,
                    final List<JobMessage> messages,
                    Clock clock, final String hostname) {
        this.jobId = jobId;
        this.jobType = jobType;
        //Truncate to milliseconds precision because current persistence implementations only support milliseconds
        this.started = started != null ? started.truncatedTo(ChronoUnit.MILLIS) : null;
        this.lastUpdated = lastUpdated != null ? lastUpdated.truncatedTo(ChronoUnit.MILLIS) : null;
        this.stopped = stopped.map(offsetDateTime -> offsetDateTime.truncatedTo(ChronoUnit.MILLIS));
        this.status = status;
        this.messages = unmodifiableList(messages);
        this.hostname = hostname;
        this.clock = clock;
    }

    /**
     * @return true if the job is finished, false, if it is still in execution.
     */
    public synchronized boolean isStopped() {
        return stopped.isPresent();
    }

    /**
     * @return the id of the job
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * @return the job type
     */
    public String getJobType() {
        return jobType;
    }

    /**
     * @return the name of the server this job is executed on
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return timestamp when the job was started
     */
    public OffsetDateTime getStarted() {
        return started;
    }

    /**
     * @return the current status of the job: OK, ERROR or DEAD
     */
    public JobStatus getStatus() {
        return status;
    }

    /**
     * @return the timestamp when the job was stopped, of empty, if the job is still running.
     */
    public Optional<OffsetDateTime> getStopped() {
        return stopped;
    }

    /**
     * @return list of job messages, containing human-readable information about what happened during execution.
     */
    public List<JobMessage> getMessages() {
        return messages;
    }

    /**
     * @return last updated timestamp
     */
    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobInfo jobInfo = (JobInfo) o;

        if (jobType != null ? !jobType.equals(jobInfo.jobType) : jobInfo.jobType != null) return false;
        if (jobId != null ? !jobId.equals(jobInfo.jobId) : jobInfo.jobId != null) return false;
        if (lastUpdated != null ? !lastUpdated.equals(jobInfo.lastUpdated) : jobInfo.lastUpdated != null) return false;
        if (messages != null ? !messages.equals(jobInfo.messages) : jobInfo.messages != null) return false;
        if (started != null ? !started.equals(jobInfo.started) : jobInfo.started != null) return false;
        if (status != jobInfo.status) return false;
        if (stopped != null ? !stopped.equals(jobInfo.stopped) : jobInfo.stopped != null) return false;
        if (hostname != null ? !hostname.equals(jobInfo.hostname) : jobInfo.hostname != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jobId != null ? jobId.hashCode() : 0;
        result = 31 * result + (jobType != null ? jobType.hashCode() : 0);
        result = 31 * result + (started != null ? started.hashCode() : 0);
        result = 31 * result + (stopped != null ? stopped.hashCode() : 0);
        result = 31 * result + (messages != null ? messages.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (lastUpdated != null ? lastUpdated.hashCode() : 0);
        result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JobInfo{" +
                "jobId=" + jobId +
                ", jobType=" + jobType +
                ", started=" + started +
                ", hostname=" + hostname +
                ", stopped=" + stopped +
                ", messages=" + messages +
                ", status=" + status +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }
    public Builder copy() {
        return new Builder(jobId, jobType, started, new ArrayList<>(messages), stopped, status, lastUpdated, hostname, clock);
    }

    public static final class Builder {
        private String jobId;
        private String jobType;
        private OffsetDateTime started;
        private List<JobMessage> messages = new ArrayList<>();
        private Clock clock;
        private OffsetDateTime stopped;
        private JobStatus status;
        private OffsetDateTime lastUpdated;
        private String hostname;
        public Builder() {
            
        }
        public Builder(String jobId, String jobType, OffsetDateTime started, List<JobMessage> messages,
                       Optional<OffsetDateTime> stopped, JobStatus status, OffsetDateTime lastUpdated, 
                       String hostname, Clock clock) {
            this.jobId = jobId;
            this.jobType = jobType;
            this.started = started;
            this.messages = messages;
            this.clock = clock;
            this.stopped = stopped.orElse(null);
            this.status = status;
            this.lastUpdated = lastUpdated;
            this.hostname = hostname;
        }

        public Builder setJobId(String jobId) {
            this.jobId = jobId;
            return this;
        }

        public Builder setClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder setJobType(String jobType) {
            this.jobType = jobType;
            return this;
        }

        public Builder setStarted(OffsetDateTime started) {
            this.started = started;
            return this;
        }

        public Builder setMessages(List<JobMessage> messages) {
            this.messages = messages;
            return this;
        }

        public Builder setStopped(OffsetDateTime stopped) {
            this.stopped = stopped;
            return this;
        }

        public Builder setStatus(JobStatus status) {
            this.status = status;
            return this;
        }

        public Builder setLastUpdated(OffsetDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public Builder setHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public JobInfo build() {
            return new JobInfo(jobId, jobType, started, lastUpdated, ofNullable(stopped), status, messages, clock, hostname);
        }

        public Builder addMessage(JobMessage jobMessage) {
            this.messages.add(jobMessage);
            return this;
        }
    }
}
