package de.otto.edison.jobs.service;

import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.eventbus.events.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

public final class JobRunner {

    private static final Logger LOG = getLogger(JobRunner.class);
    public static final long PING_PERIOD = 20l;

    private final JobEventPublisher jobEventPublisher;
    private final URI jobUri;
    private final String jobType;
    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> pingJob;

    private JobRunner(final URI jobUri,
                      final String jobType,
                      final ScheduledExecutorService executorService,
                      final JobEventPublisher jobEventPublisher) {
        this.jobUri = jobUri;
        this.jobType = jobType;
        this.executorService = executorService;
        this.jobEventPublisher = jobEventPublisher;
        jobEventPublisher.stateChanged(CREATE);
    }

    public static JobRunner newJobRunner(final URI jobUri,
                                         final String jobType,
                                         final ScheduledExecutorService executorService,
                                         final JobEventPublisher jobEventPublisher) {
        final JobRunner jobRunner = new JobRunner(jobUri, jobType, executorService, jobEventPublisher);
        return jobRunner;
    }

    public void start(final JobRunnable runnable) {
        start();
        try {
            final int restarts = runnable.getJobDefinition().restarts();
            executeAndRetry(runnable, restarts);
        } catch (final RuntimeException e) {
            error(e);
        } finally {
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
                LOG.warn("Retrying job ");
                executeAndRetry(runnable, restarts - 1);
            }
        }
    }

    private synchronized void start() {
        jobEventPublisher.stateChanged(START);
        pingJob = executorService.scheduleAtFixedRate(this::ping, PING_PERIOD, PING_PERIOD, SECONDS);

        final String jobId = jobUri.toString();
        MDC.put("job_id", jobId.substring(jobId.lastIndexOf('/') + 1));
        MDC.put("job_type", jobType);
        LOG.info("[started]");
    }

    public void ping() {
        try {
            jobEventPublisher.stateChanged(STILL_ALIVE);
        } catch (Exception e) {
            LOG.error("Fatal error in ping job for" + jobType + " (" + jobUri + ")", e);
        }
    }

    private synchronized void error(final Exception e) {
        jobEventPublisher.message(MessageEvent.Level.ERROR, "Fatal error in job " + jobType + " (" + jobUri + ") " + e.getMessage());
        LOG.error("Fatal error in job " + jobType + " (" + jobUri + ")", e.getMessage());
    }

    private synchronized void stop() {
        pingJob.cancel(false);

        try {
            jobEventPublisher.stateChanged(STOP);
            LOG.info("stopped job {}", jobUri);
        } finally {
            MDC.clear();
        }
    }
}
