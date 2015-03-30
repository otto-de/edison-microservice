package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.ERROR;
import static de.otto.edison.jobs.domain.JobInfoBuilder.copyOf;
import static de.otto.edison.jobs.domain.JobMessage.*;
import static java.time.OffsetDateTime.now;
import static org.slf4j.LoggerFactory.getLogger;

public final class JobRunner {

    private static final Logger LOG = getLogger(JobRunner.class);
    public static final long PING_PERIOD = 10l;

    private final JobRepository repository;
    private volatile JobInfo job;
    private final Clock clock;
    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> pingJob;

    private JobRunner(final JobInfo job, final JobRepository repository, final Clock clock, final ScheduledExecutorService executorService) {
        this.repository = repository;
        this.job = job;
        this.clock = clock;
        this.executorService = executorService;
    }

    public static JobRunner newJobRunner(final JobInfo job, final JobRepository repository, final Clock clock, final ScheduledExecutorService executorService) {
        return new JobRunner(job, repository, clock, executorService);
    }

    public void start(final JobRunnable runnable) {
        start();
        try {
            runnable.execute(this::log);
        } catch (final RuntimeException e) {
            error(e);
        } finally {
            stop();
        }
    }

    private void log(final JobMessage jobMessage) {
        synchronized (this) {
            switch (jobMessage.getLevel()) {
                case WARNING:
                    LOG.warn(jobMessage.getMessage());
                    break;
                default:
                    LOG.info(jobMessage.getMessage());
            }

            job = copyOf(job).addMessage(jobMessage)
                    .build();
            createOrUpdateJob();
        }
    }

    private void start() {
        pingJob = executorService.scheduleAtFixedRate(this::ping, PING_PERIOD, PING_PERIOD, TimeUnit.SECONDS);

        final String jobId = job.getJobUri().toString();
        MDC.put("job_id", jobId.substring(jobId.lastIndexOf('/') + 1));
        MDC.put("job_type", job.getJobType().toString());
        createOrUpdateJob();
        LOG.info("[started]");
    }

    private void ping() {
        synchronized (this) {
            job = copyOf(job)
                    .addMessage(jobMessage(Level.INFO, "Job still alive."))
                    .build();

            createOrUpdateJob();
        }
    }

    private void error(final Exception e) {
        synchronized (this) {
            assert !job.getStopped().isPresent();
            job = copyOf(job).withStatus(ERROR).build();

            LOG.error("Fatal error in job "+ job.getJobType()+" ("+job.getJobUri()+")",e);
            log(jobMessage(Level.WARNING,e.getMessage()));

            createOrUpdateJob();
        }
    }

    private void stop() {
        synchronized (this) {
            pingJob.cancel(false);

            assert !job.getStopped().isPresent();
            try {
                LOG.info("stopped job {}", job);
                job = copyOf(job)
                        .withStopped(now())
                        .build();
                createOrUpdateJob();
            } finally {
                MDC.clear();
            }
        }
    }

    private void createOrUpdateJob() {
        synchronized (this) {
            job = copyOf(job)
                    .withLastUpdated(clock.now())
                    .build();

            repository.createOrUpdate(job);

        }
    }

}
