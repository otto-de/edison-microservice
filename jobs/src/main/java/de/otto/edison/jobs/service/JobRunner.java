package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.MDC;

import static de.otto.edison.jobs.domain.JobInfo.ExecutionState.RUNNING;
import static de.otto.edison.jobs.domain.JobInfo.ExecutionState.STOPPED;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.ERROR;
import static de.otto.edison.jobs.domain.JobInfoBuilder.copyOf;
import static java.time.OffsetDateTime.now;
import static org.slf4j.LoggerFactory.getLogger;

public final class JobRunner {

    private static final Logger LOG = getLogger(JobRunner.class);

    private final JobRepository repository;
    private volatile JobInfo job;
    private final Clock clock;

    private JobRunner(final JobInfo job, final JobRepository repository, final Clock clock) {
        this.repository = repository;
        this.job = job;
        this.clock = clock;
    }

    public static JobRunner newJobRunner(final JobInfo job, final JobRepository repository, final Clock clock) {
        return new JobRunner(job, repository, clock);
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
        job = copyOf(job).addMessage(jobMessage)
                .build();
        createOrUpdateJob();
    }

    private void start() {
        final String jobId = job.getJobUri().toString();
        MDC.put("job_id", jobId.substring(jobId.lastIndexOf('/') + 1));
        MDC.put("job_type", job.getJobType().toString());
        createOrUpdateJob();
        LOG.info("[started]");
    }

    private void error(final Exception e) {
        assert job.getState() == RUNNING;
        job = copyOf(job).withStatus(ERROR).build();
        LOG.error(e.getMessage());
        createOrUpdateJob();
    }

    private void stop() {
        assert job.getState() == RUNNING;
        try {
            LOG.info("[stopped]");
            job = copyOf(job)
                    .withState(STOPPED)
                    .withStopped(now())
                    .build();
            createOrUpdateJob();
        } finally {
            MDC.clear();
        }
    }

    private void createOrUpdateJob() {
        job = copyOf(job)
                .withLastUpdated(clock.now())
                .build();

        repository.createOrUpdate(job);
    }

}
