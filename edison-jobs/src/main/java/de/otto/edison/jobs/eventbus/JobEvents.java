package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.domain.Level;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Delegates the calls of {@link JobEventPublisher#info(String)},
 * {@link JobEventPublisher#warn(String)} and {@link JobEventPublisher#error(String)}
 * to the correct JobEventPublisher of a job.
 *
 * This can be useful in methods outside the {@link de.otto.edison.jobs.service.JobRunnable#execute(JobEventPublisher)}
 * method which do not know about the JobEventPublisher of a Job (e.g. stateless dependencies of a job).
 */
public final class JobEvents {

    private static final ThreadLocal<JobEventPublisher> jobEventPublisherThreadLocal = new InheritableThreadLocal<>();
    private static final Set<JobEventPublisher> jobEventPublishers = new LinkedHashSet<>();

    /**
     * Internal method. Should only be called inside edison-jobs.
     *
     * @param jobEventPublisher the JobEventPublisher to register
     */
    public static void register(final JobEventPublisher jobEventPublisher) {
        if (jobEventPublisherThreadLocal.get() != null) {
            throw new IllegalStateException(
                    "JobEventPublisher has already been initialised. " +
                    "Either you forgot to call destroy(deregister) or you called register(JobEventPublisher) twice"
            );
        }
        jobEventPublisherThreadLocal.set(jobEventPublisher);
        jobEventPublishers.add(jobEventPublisher);
    }

    /**
     * Internal method. Should only be called inside edison-jobs.
     */
    public static void deregister() {
        JobEventPublisher jobEventPublisher = jobEventPublisherThreadLocal.get();
        if (jobEventPublisher != null) {
            jobEventPublishers.remove(jobEventPublisher);
        }
        jobEventPublisherThreadLocal.remove();
    }

    /**
     * Publish an error event.
     *
     * @param message error message
     */
    public static void error(final String message) {
        checkInitialisation();
        jobEventPublisherThreadLocal.get().error(message);
    }

    /**
     * Publish a warning event.
     *
     * @param message warning message
     */
    public static void warn(final String message) {
        checkInitialisation();
        jobEventPublisherThreadLocal.get().warn(message);
    }

    /**
     * Publish an info event.
     *
     * @param message info message
     */
    public static void info(final String message) {
        checkInitialisation();
        jobEventPublisherThreadLocal.get().info(message);
    }

    /**
     * Publishes an event with the given level to all currently running jobs
     *
     * @param level
     * @param message
     */
    public static void broadcast(final Level level, final String message) {
        for (JobEventPublisher jobEventPublisher : jobEventPublishers) {
            jobEventPublisher.message(level, message);
        }
    }

    private static void checkInitialisation() {
        if (jobEventPublisherThreadLocal.get() == null) {
            throw new IllegalStateException("JobEventPublisher has not been initialised. Try calling JobEvents.register(JobEventPublisher) first");
        }
    }

    private JobEvents() {
        // should not be instantiated
    }
}
