package de.otto.edison.jobs.definition;

import java.time.Duration;
import java.util.Optional;

/**
 * Definition about how often and when a job expects to be triggered, restarted, and so on.
 *
 * This information could be used by external triggers and during execution
 * of jobs.
 *
 * @author Guido Steinacker
 * @since 25.08.15
 */
public interface JobDefinition {

    /**
     * The type of the job that is specified by this JobDefinition.
     *
     * Only one JobDefinition per type is supported.
     *
     * @return job type
     */
    public String jobType();

    /**
     * A human-readable name of the job.
     *
     * @return job name
     */
    public String jobName();

    /**
     * A human-readable description of the job.
     *
     * @return job description
     */
    public String description();

    /*
     * The optional maximum duration after that a job is regarded as too old and a warning is indicated.
     *
     * @return max age of a job
     */
    public default Optional<Duration> maxAge() { return Optional.empty(); }

    /**
     * Optional fixed delay after that a job is triggered again.
     *
     * @return optional fixed delay
     */
    public default Optional<Duration> fixedDelay() { return Optional.empty(); }

    /**
     * Optional cron expression used to specify when a job should be triggered.
     *
     * @return optional cron expression
     */
    public default Optional<String> cron() { return Optional.empty(); }

    /**
     * Specifies how often the job should be restarted by the {@link de.otto.edison.jobs.service.JobRunner} if it failed because of errors or exceptions.
     *
     * By default, jobs are not restarted.
     *
     * @return number of restarts after errors.
     */
    public default int restarts() { return 0; }

    /**
     * Number of retries when starting a job is failing because it is already running or blocked by other jobs.
     *
     * This information is used by a job trigger to determine, how often the job should be retriggered, if
     * the job failed to start.
     *
     * @return number of retries
     */
    public default int retries() { return 0; }

    /**
     * The duration after that a retry should be scheduled.
     *
     * This information is used by a job trigger to determine, how long a retry should be delayed.
     *
     * @return optional delay before retrying
     */
    public default Optional<Duration> retryDelay() { return Optional.empty(); }
}
