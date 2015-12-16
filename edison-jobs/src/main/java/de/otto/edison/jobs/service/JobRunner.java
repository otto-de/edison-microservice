package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.eventbus.EventPublisher;
import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.ERROR;
import static de.otto.edison.jobs.eventbus.events.MessageEvent.Level.INFO;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.START;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.STILL_ALIVE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

public final class JobRunner {

    private static final Logger LOG = getLogger(JobRunner.class);
    public static final long PING_PERIOD = 1l;

    private EventPublisher eventPublisher;
    private volatile JobInfo jobInfo;
    private final JobRepository jobRepository;
    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> pingJob;

    private JobRunner(final JobInfo jobInfo,
                      final JobRepository jobRepository,
                      final ScheduledExecutorService executorService,
                      final EventPublisher eventPublisher) {
        this.jobInfo = jobInfo;
        this.jobRepository = jobRepository;
        this.executorService = executorService;
        this.eventPublisher = eventPublisher;
    }

    public static JobRunner newJobRunner(final JobInfo job,
                                         final JobRepository jobRepository,
                                         final ScheduledExecutorService executorService,
                                         final EventPublisher eventPublisher) {
        final JobRunner jobRunner = new JobRunner(job, jobRepository, executorService, eventPublisher);
        jobRepository.createOrUpdate(jobRunner.jobInfo);
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
            runnable.execute(jobInfo, eventPublisher);
        } catch (final RuntimeException e) {
            error(e);
        }
        if (jobInfo.getStatus() == ERROR && restarts > 0) {
            restart();
            executeAndRetry(runnable, restarts - 1);
        }
    }

    private synchronized void start() {
        pingJob = executorService.scheduleAtFixedRate(this::ping, PING_PERIOD, PING_PERIOD, SECONDS);

        final String jobId = jobInfo.getJobUri().toString();
        MDC.put("job_id", jobId.substring(jobId.lastIndexOf('/') + 1));
        MDC.put("job_type", jobInfo.getJobType());
        LOG.info("[started]");
        eventPublisher.stateChanged(this, jobInfo.getJobUri(), START);
    }

    private synchronized void ping() {
        try {
            if (jobRepository.findStatus(jobInfo.getJobUri()).equals(JobInfo.JobStatus.DEAD)) {
                jobInfo.dead();
            }
            eventPublisher.stateChanged(this, jobInfo.getJobUri(), STILL_ALIVE);
            jobInfo.ping();
            jobRepository.createOrUpdate(jobInfo);
        } catch (Exception e) {
            assert !jobInfo.isStopped();
            LOG.error("Fatal error in ping job for" + jobInfo.getJobType() + " (" + jobInfo.getJobUri() + ")", e);
        }
    }

    private synchronized void error(final Exception e) {
        assert !jobInfo.isStopped();
        jobInfo.error(e.getMessage());
        jobRepository.createOrUpdate(jobInfo);
        eventPublisher.message(this, jobInfo.getJobUri(), MessageEvent.Level.ERROR, "Fatal error in job " + jobInfo.getJobType() + " (" + jobInfo.getJobUri() + ") " + e.getMessage());
        LOG.error("Fatal error in job " + jobInfo.getJobType() + " (" + jobInfo.getJobUri() + ")", e);
    }

    private synchronized void restart() {
        jobInfo.restart();
        jobRepository.createOrUpdate(jobInfo);
        LOG.warn("Retrying job ");
        eventPublisher.message(this, jobInfo.getJobUri(), INFO, "restarting job ..");
    }

    private synchronized void stop() {
        pingJob.cancel(false);

        assert !jobInfo.isStopped();
        try {
            LOG.info("stopped job {}", jobInfo);
            jobInfo.stop();
            jobRepository.createOrUpdate(jobInfo);
        } finally {
            MDC.clear();
        }
    }
}
