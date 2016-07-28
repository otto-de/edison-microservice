package de.otto.edison.jobs.eventbus;

public class JobEvents {

    public static ThreadLocal<JobEventPublisher> jobEventPublisherThreadLocal = new InheritableThreadLocal<>();

    /**
     * Internal method. Should only be called inside edison-jobs.
     */
    public static void register(JobEventPublisher jobEventPublisher) {
        if (JobEvents.jobEventPublisherThreadLocal.get() != null) {
            throw new IllegalStateException("JobEventPublisher has already been initialised. " +
                    "Either you forgot to call destroy() or you called init() twice");
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
        if(jobEventPublisherThreadLocal.get() == null) {
            throw new IllegalStateException("JobEventPublisher has not been initialised. Try calling JobEvents.init() first");
        }
    }
}
