package de.otto.edison.jobs.definition;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;

/**
 * @author Guido Steinacker
 * @since 25.08.15
 */
public interface JobDefinition {

    /**
     * The URL of the job trigger.
     *
     * Jobs can be triggered by sending an HTTP POST to the specified URL.
     *
     * @return job trigger URL
     */
    public URL triggerUrl();

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
    public default Optional<Duration> maxAge() { return Optional.empty(); };

    /**
     * Optional fixed delay after that a job is triggered again.
     *
     * @return optional fixed delay
     */
    public default Optional<Duration> fixedDelay() { return Optional.empty(); };

    /**
     * Optional cron expression used to specify when a job should be triggered.
     *
     * @return optional cron expression
     */
    public default Optional<String> cron() { return Optional.empty(); };

    /**
     * Number of retries when starting a job is failing for some reason.
     *
     * @return number of retries
     */
    public default int retries() { return 0; };

    /**
     * The duration after that a retry should be scheduled.
     *
     * @return optional delay before retrying
     */
    public default Optional<Duration> retryDelay() { return Optional.empty(); };
}
