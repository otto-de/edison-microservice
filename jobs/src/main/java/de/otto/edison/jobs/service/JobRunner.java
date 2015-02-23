package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;

import static de.otto.edison.jobs.domain.JobInfo.ExecutionState.RUNNING;
import static de.otto.edison.jobs.domain.JobInfo.ExecutionState.STOPPED;
import static java.time.LocalDateTime.now;
import static org.slf4j.LoggerFactory.getLogger;

public final class JobRunner {

    private static final Logger JOB_LOGGER = getLogger(JobRunner.class);

    private final JobRepository repository;
    private final JobInfo job;

    private JobRunner(final JobInfo job, final JobRepository repository) {
        this.repository = repository;
        this.job = job;
    }

    public static JobRunner newJobRunner(final JobInfo job, final JobRepository repository) {
        return new JobRunner(job, repository);
    }

    @Async
    public void startAsync(final JobRunnable runnable) {
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
        job.addMessage(jobMessage);
        repository.createOrUpdate(job);
    }

    private void start() {
        final String jobId = job.getJobUri().toString();
        MDC.put("job_id", jobId.substring(jobId.lastIndexOf('/') + 1));
        MDC.put("job_type", job.getJobType().toString());
        repository.createOrUpdate(job);
        JOB_LOGGER.info("[started]");
    }

    private void error(final Exception e) {
        assert job.getState() == RUNNING;
        job.setStatus(JobInfo.JobStatus.ERROR);
        JOB_LOGGER.error(e.getMessage());
        repository.createOrUpdate(job);
    }

    private void stop() {
        assert job.getState() == RUNNING;
        try {
            JOB_LOGGER.info("[stopped]");
            job.setState(STOPPED);
            job.setStopped(now());
            repository.createOrUpdate(job);
        } finally {
            MDC.clear();
        }
    }

}
