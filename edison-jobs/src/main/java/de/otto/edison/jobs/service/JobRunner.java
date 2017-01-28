package de.otto.edison.jobs.service;

import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.eventbus.JobEvents;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

public final class JobRunner {

    private static final Logger LOG = getLogger(JobRunner.class);
    public static final long PING_PERIOD = 20l;

    private final JobEventPublisher jobEventPublisher;
    private final String jobId;
    private final String jobType;
    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> pingJob;

    private JobRunner(final String jobId,
                      final String jobType,
                      final ScheduledExecutorService executorService,
                      final JobEventPublisher jobEventPublisher) {
        this.jobId = jobId;
        this.jobType = jobType;
        this.executorService = executorService;
        this.jobEventPublisher = jobEventPublisher;
    }

    public static JobRunner newJobRunner(final String jobId,
                                         final String jobType,
                                         final ScheduledExecutorService executorService,
                                         final JobEventPublisher jobEventPublisher) {
        return new JobRunner(jobId, jobType, executorService, jobEventPublisher);
    }

    public void start(final JobRunnable runnable) {
        start();
        try {
            JobEvents.register(jobEventPublisher);
            final int restarts = runnable.getJobDefinition().restarts();
            executeAndRetry(runnable, restarts);
        } catch (final RuntimeException e) {
            error(e);
        } finally {
            JobEvents.deregister();
            stop();
        }
    }

    private synchronized void executeAndRetry(final JobRunnable runnable, final int restarts) {
        try {
            runnable.execute(jobEventPublisher);
        } catch (final RuntimeException e) {
            error(e);
            if (restarts > 0) {
                jobEventPublisher.stateChanged(RESTART);
                executeAndRetry(runnable, restarts - 1);
            }
        }
    }

    private synchronized void start() {
        jobEventPublisher.stateChanged(START);
        pingJob = executorService.scheduleAtFixedRate(this::ping, PING_PERIOD, PING_PERIOD, SECONDS);

        MDC.put("job_id", jobId.substring(jobId.lastIndexOf('/') + 1));
        MDC.put("job_type", jobType);
        LOG.info("job started '{}'", jobId);
    }

    public void ping() {
        try {
            jobEventPublisher.stateChanged(KEEP_ALIVE);
        } catch (Exception e) {
            LOG.error("Fatal error in ping job for" + jobType + " (" + jobId + ")", e);
        }
    }

    private synchronized void error(final Exception e) {
        StringWriter stacktrace = new StringWriter();
        PrintWriter pw = new PrintWriter(stacktrace);
        e.printStackTrace(pw);
        String message = String.format("Fatal error in job %s (%s)\n%s", jobType, jobId, stacktrace.toString());
        jobEventPublisher.error(message);
        LOG.error(String.format("Fatal error in job %s (%s)", jobType, jobId), e);
    }

    private synchronized void stop() {
        pingJob.cancel(false);

        try {
            jobEventPublisher.stateChanged(STOP);
            LOG.info("job stopped '{}'", jobId);
        } finally {
            MDC.clear();
        }
    }
}
