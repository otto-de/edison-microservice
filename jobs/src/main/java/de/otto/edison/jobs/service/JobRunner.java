package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

public final class JobRunner {

    private static final Logger LOG = getLogger(JobRunner.class);
    public static final long PING_PERIOD = 1l;

    private volatile JobInfo jobInfo;
    private final JobRepository jobRepository;
    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> pingJob;

    private JobRunner(final JobInfo jobInfo,
                      final JobRepository jobRepository,
                      final ScheduledExecutorService executorService) {
        this.jobInfo = jobInfo;
        this.jobRepository = jobRepository;
        this.executorService = executorService;
    }

    public static JobRunner newJobRunner(final JobInfo job,
                                         final JobRepository jobRepository,
                                         final ScheduledExecutorService executorService) {
        final JobRunner jobRunner = new JobRunner(job, jobRepository, executorService);
        jobRepository.createOrUpdate(jobRunner.jobInfo);
        return jobRunner;
    }

    public void start(final JobRunnable runnable) {
        start();
        try {
            runnable.execute(jobInfo);
        } catch (final RuntimeException e) {
            error(e);
        } finally {
            stop();
        }
    }

    private void start() {
        synchronized (this) {
            pingJob = executorService.scheduleAtFixedRate(this::ping, PING_PERIOD, PING_PERIOD, SECONDS);

            final String jobId = jobInfo.getJobUri().toString();
            MDC.put("job_id", jobId.substring(jobId.lastIndexOf('/') + 1));
            MDC.put("job_type", jobInfo.getJobType());
            LOG.info("[started]");
        }
    }

    private void ping() {
        synchronized (this) {
            jobInfo.ping();
            jobRepository.createOrUpdate(jobInfo);
        }
    }

    private void error(final Exception e) {
        synchronized (this) {
            assert !jobInfo.isStopped();
            jobInfo.error(e.getMessage());
            jobRepository.createOrUpdate(jobInfo);
            LOG.error("Fatal error in job "+ jobInfo.getJobType()+" ("+ jobInfo.getJobUri()+")",e);
        }
    }

    private void stop() {
        synchronized (this) {
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

}
