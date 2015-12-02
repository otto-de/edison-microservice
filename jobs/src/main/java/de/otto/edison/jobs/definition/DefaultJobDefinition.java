package de.otto.edison.jobs.definition;

import java.time.Duration;
import java.util.Optional;

/**
 * Immutable implementation of a JobDefinition.
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
    private final int restarts;
    private final int retries;
    private final Optional<Duration> retryDelay;

    /**
     * Create a JobDefinition for a job that will not be triggered automatically by a job trigger.
     *
     * @param jobType     The type of the Job
     * @param jobName     A human readable name of the Job
     * @param description A short description of the job's purpose
     * @param restarts    The number of restarts if the job failed because of errors or exceptions
     * @param maxAge      Optional maximum age of a job. After this duration, the job is marked as dead
     *
     * @return JobDefinition
     */
    public static JobDefinition notTriggerableJobDefinition(final String jobType,
                                                            final String jobName,
                                                            final String description,
                                                            final int restarts,
                                                            final Optional<Duration> maxAge) {
        return new DefaultJobDefinition(jobType, jobName, description, maxAge, Optional.empty(), Optional.empty(), restarts, 0, Optional.empty());
    }

    /**
     * Create a JobDefinition that is using a cron expression to specify, when and how often the job should be triggered.
     *
     * @param jobType     The type of the Job
     * @param jobName     A human readable name of the Job
     * @param description A human readable description of the Job.
     * @param cron        The cron expression
     * @param restarts    The number of restarts if the job failed because of errors or exceptions
     * @param maxAge      Optional maximum age of a job. After this duration, the job is marked as dead
     *
     * @return JobDefinition
     */
    public static JobDefinition cronJobDefinition(final String jobType,
                                                  final String jobName,
                                                  final String description,
                                                  final String cron,
                                                  final int restarts,
                                                  final Optional<Duration> maxAge) {
        return new DefaultJobDefinition(jobType, jobName, description, maxAge, Optional.empty(), Optional.of(cron), restarts, 0, Optional.empty());
    }

    /**
     * Create a JobDefinition that is using a cron expression to specify, when and how often the job should be triggered.
     *
     * @param jobType     The type of the Job
     * @param jobName     A human readable name of the Job
     * @param description A human readable description of the Job.
     * @param cron        The cron expression
     * @param maxAge      Maximum age of the latest job after that we want to get a warning
     * @param restarts    The number of restarts if the job failed because of errors or exceptions
     * @param retries     Specifies how often a job trigger should retry to start the job if triggering fails for some reason.
     * @param retryDelay  The optional delay between retries.
     * @param maxAge      Optional maximum age of a job. After this duration, the job is marked as dead
     *
     * @return JobDefinition
     */
    public static JobDefinition retryableCronJobDefinition(final String jobType,
                                                           final String jobName,
                                                           final String description,
                                                           final String cron,
                                                           final int restarts,
                                                           final int retries,
                                                           final Duration retryDelay,
                                                           final Optional<Duration> maxAge) {
        return new DefaultJobDefinition(jobType, jobName, description, maxAge, Optional.empty(), Optional.of(cron), restarts, retries, Optional.of(retryDelay));
    }

    /**
     * Create a JobDefinition that is using fixed delays specify, when and how often the job should be triggered.
     *
     * @param jobType     The type of the Job
     * @param jobName     A human readable name of the Job
     * @param description A human readable description of the Job.
     * @param fixedDelay  The delay duration between to executions of the Job
     * @param restarts    The number of restarts if the job failed because of errors or exceptions
     * @param maxAge      Optional maximum age of a job. After this duration, the job is marked as dead
     * @return JobDefinition
     */
    public static DefaultJobDefinition fixedDelayJobDefinition(final String jobType,
                                                               final String jobName,
                                                               final String description,
                                                               final Duration fixedDelay,
                                                               final int restarts,
                                                               final Optional<Duration> maxAge) {
        return new DefaultJobDefinition(jobType, jobName, description, maxAge, Optional.of(fixedDelay), Optional.empty(), restarts, 0, Optional.empty());
    }

    /**
     * Create a JobDefinition that is using fixed delays specify, when and how often the job should be triggered.
     *
     * @param jobType     The type of the Job
     * @param jobName     A human readable name of the Job
     * @param description A human readable description of the Job.
     * @param fixedDelay  The delay duration between to executions of the Job
     * @param restarts    The number of restarts if the job failed because of errors or exceptions
     * @param maxAge      Optional maximum age of a job. After this duration, the job is marked as dead
     * @param retries     Specifies how often a job trigger should retry to start the job if triggering fails for some reason.
     * @param retryDelay  The optional delay between retries.
     * @return JobDefinition
     */
    public static DefaultJobDefinition retryableFixedDelayJobDefinition(final String jobType,
                                                                        final String jobName,
                                                                        final String description,
                                                                        final Duration fixedDelay,
                                                                        final int restarts,
                                                                        final int retries,
                                                                        final Optional<Duration> retryDelay,
                                                                        final Optional<Duration> maxAge) {
        return new DefaultJobDefinition(jobType, jobName, description, maxAge, Optional.of(fixedDelay), Optional.empty(), restarts, retries, retryDelay);
    }

    private DefaultJobDefinition(final String jobType,
                                 final String jobName,
                                 final String description,
                                 final Optional<Duration> maxAge,
                                 final Optional<Duration> fixedDelay,
                                 final Optional<String> cron,
                                 final int restarts,
                                 final int retries,
                                 final Optional<Duration> retryDelay) {
        this.jobType = jobType;
        this.jobName = jobName;
        this.description = description;
        this.maxAge = maxAge;
        this.fixedDelay = fixedDelay;
        this.cron = cron;
        this.restarts = restarts;
        this.retries = retries;
        this.retryDelay = retryDelay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String jobType() {
        return jobType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String jobName() {
        return jobName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Duration> maxAge() {
        return maxAge;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Duration> fixedDelay() {
        return fixedDelay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> cron() {
        return cron;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int restarts() {
        return restarts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int retries() {
        return retries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Duration> retryDelay() {
        return retryDelay;
    }
}
