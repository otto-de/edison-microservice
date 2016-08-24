package de.otto.edison.jobs.eventbus;

import de.otto.edison.annotations.Beta;

/**
 * Delegates the calls of {@link JobEventPublisher#info(String)},
 * {@link JobEventPublisher#warn(String)} and {@link JobEventPublisher#error(String)}
 * to the correct JobEventPublisher of a job.
 * This can be useful in methods outside the {@link de.otto.edison.jobs.service.JobRunnable#execute(JobEventPublisher)} method which do not know about the JobEventPublisher of a Job (e.g. stateless dependencies of a job).
 */
@Beta
public class JobEvents {

    public static ThreadLocal<JobEventPublisher> jobEventPublisherThreadLocal = new InheritableThreadLocal<>();

    /**
     * Internal method. Should only be called inside edison-jobs.
     */
    public static void register(JobEventPublisher jobEventPublisher) {
        if (JobEvents.jobEventPublisherThreadLocal.get() != null) {
            throw new IllegalStateException("JobEventPublisher has already been initialised. " +
                    "Either you forgot to call destroy(deregister) or you called register(JobEventPublisher) twice");
        }
        JobEvents.jobEventPublisherThreadLocal.set(jobEventPublisher);
    }

    /**
     * Internal method. Should only be called inside edison-jobs.
     */
    public static void deregister() {
        JobEvents.jobEventPublisherThreadLocal.remove();
    }

    public static void error(String message) {
        checkInitialisation();
        jobEventPublisherThreadLocal.get().error(message);
    }

    public static void warn(String message) {
        checkInitialisation();
        jobEventPublisherThreadLocal.get().warn(message);
    }

    public static void info(String message) {
        checkInitialisation();
        jobEventPublisherThreadLocal.get().info(message);
    }

    private static void checkInitialisation() {
        if (jobEventPublisherThreadLocal.get() == null) {
            throw new IllegalStateException("JobEventPublisher has not been initialised. Try calling JobEvents.register(JobEventPublisher) first");
        }
    }
}
