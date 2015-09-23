package de.otto.edison.jobs.definition;

import java.time.Duration;
import java.util.Optional;

/**
 * A definition of a job type.
 *
 * @author Guido Steinacker
 * @since 20.08.15
 */
public final class DefaultJobDefinition implements JobDefinition {

    private final String jobType;
    private final String jobName;
    private final String description;
    private final Optional<Duration> maxAge;
    private final Optional<Duration> fixedDelay;
    private final Optional<String> cron;
    private final int retries;
    private final Optional<Duration> retryDelay;

    /**
     * Create a JobDefinition that is using a cron expression to specify, when and how often the job should be triggered.
     *
     * @param jobType     The type of the Job
     * @param jobName     A human readable name of the Job
     * @param description A human readable description of the Job.
     * @param cron        The cron expression
     * @param maxAge      Maximum age of the latest job after that we want to get a warning
     * @return JobDefinition
     */
    public static JobDefinition cronJobDefinition(final String jobType,
                                                  final String jobName,
                                                  final String description,
                                                  final String cron,
                                                  final Optional<Duration> maxAge) {
        return new DefaultJobDefinition(jobType, jobName, description, maxAge, Optional.empty(), Optional.of(cron), 0, Optional.empty());
    }

    /**
     * Create a JobDefinition that will not be triggered.
     *
     * @param jobType     The type of the Job
     * @param jobName     A human readable name of the Job
     * @param description A human readable description of the Job.
     * @return JobDefinition
     */
    public static JobDefinition notTriggerableJobDefinition(final String jobType,
                                                            final String jobName,
                                                            final String description) {
        return new DefaultJobDefinition(jobType, jobName, description, Optional.empty(), Optional.empty(), Optional.empty(), 0, Optional.empty());
    }

    /**
     * Create a JobDefinition that is using a cron expression to specify, when and how often the job should be triggered.
     *
     * @param jobType     The type of the Job
     * @param jobName     A human readable name of the Job
     * @param description A human readable description of the Job.
     * @param cron        The cron expression
     * @param maxAge      Maximum age of the latest job after that we want to get a warning
     * @param retries     Specifies how often a job trigger should retry to start the job if triggering fails for some reason.
     * @param retryDelay  The optional delay between retries.
     * @return JobDefinition
     */
    public static JobDefinition retryableCronJobDefinition(final String jobType,
                                                           final String jobName,
                                                           final String description,
                                                           final String cron,
                                                           final int retries,
                                                           final Duration retryDelay,
                                                           final Optional<Duration> maxAge) {
        return new DefaultJobDefinition(jobType, jobName, description, maxAge, Optional.empty(), Optional.of(cron), retries, Optional.of(retryDelay));
    }

    /**
     * Create a JobDefinition that is using fixed delays specify, when and how often the job should be triggered.
     *
     * @param jobType     The type of the Job
     * @param jobName     A human readable name of the Job
     * @param description A human readable description of the Job.
     * @param fixedDelay  The delay duration between to executions of the Job
     * @param maxAge      Maximum age of the latest job after that we want to get a warning
     * @return JobDefinition
     */
    public static DefaultJobDefinition fixedDelayJobDefinition(final String jobType,
                                                               final String jobName,
                                                               final String description,
                                                               final Duration fixedDelay,
                                                               final Optional<Duration> maxAge) {
        return new DefaultJobDefinition(jobType, jobName, description, maxAge, Optional.of(fixedDelay), Optional.empty(), 0, Optional.empty());
    }

    /**
     * Create a JobDefinition that is using fixed delays specify, when and how often the job should be triggered.
     *
     * @param jobType     The type of the Job
     * @param jobName     A human readable name of the Job
     * @param description A human readable description of the Job.
     * @param fixedDelay  The delay duration between to executions of the Job
     * @param maxAge      Maximum age of the latest job after that we want to get a warning
     * @param retries     Specifies how often a job trigger should retry to start the job if triggering fails for some reason.
     * @param retryDelay  The optional delay between retries.
     * @return JobDefinition
     */
    public static DefaultJobDefinition retryableFixedDelayJobDefinition(final String jobType,
                                                                        final String jobName,
                                                                        final String description,
                                                                        final Duration fixedDelay,
                                                                        final int retries,
                                                                        final Optional<Duration> retryDelay,
                                                                        final Optional<Duration> maxAge) {
        return new DefaultJobDefinition(jobType, jobName, description, maxAge, Optional.of(fixedDelay), Optional.empty(), retries, retryDelay);
    }

    private DefaultJobDefinition(final String jobType,
                                 final String jobName,
                                 final String description,
                                 final Optional<Duration> maxAge,
                                 final Optional<Duration> fixedDelay,
                                 final Optional<String> cron,
                                 final int retries,
                                 final Optional<Duration> retryDelay) {
        this.jobType = jobType;
        this.jobName = jobName;
        this.description = description;
        this.maxAge = maxAge;
        this.fixedDelay = fixedDelay;
        this.cron = cron;
        this.retries = retries;
        this.retryDelay = retryDelay;
    }

    /**
     * The type of the job that is specified by this JobDefinition.
     * <p>
     * Only one JobDefinition per type is supported.
     *
     * @return job type
     */
    @Override
    public String jobType() {
        return jobType;
    }

    /**
     * A human-readable name of the job.
     *
     * @return job name
     */
    @Override
    public String jobName() {
        return jobName;
    }

    /**
     * A human-readable description of the job.
     *
     * @return job description
     */
    @Override
    public String description() {
        return description;
    }

    /*
     * The optional maximum duration after that a job is regarded as too old and a warning is indicated.
     *
     * @return max age of a job
     */
    @Override
    public Optional<Duration> maxAge() {
        return maxAge;
    }

    /**
     * Optional fixed delay after that a job is triggered again.
     *
     * @return optional fixed delay
     */
    @Override
    public Optional<Duration> fixedDelay() {
        return fixedDelay;
    }

    /**
     * Optional cron expression used to specify when a job should be triggered.
     *
     * @return optional cron expression
     */
    @Override
    public Optional<String> cron() {
        return cron;
    }

    /**
     * Number of retries when starting a job is failing for some reason.
     *
     * @return number of retries
     */
    @Override
    public int retries() {
        return retries;
    }

    /**
     * The duration after that a retry should be scheduled.
     */
    @Override
    public Optional<Duration> retryDelay() {
        return retryDelay;
    }
}
