package de.otto.edison.jobs.repository.cleanup;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static java.lang.String.format;
import static java.time.OffsetDateTime.now;

public class StopDeadJobs implements JobCleanupStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(StopDeadJobs.class);
    private static final long STOP_DEAD_JOBS_CLEANUP_INTERVAL = 10L * 60L * 1000L;


    private final int stopJobAfterSeconds;
    private final Clock clock;
    private final JobService jobService;
    private final JobRepository jobRepository;

    public StopDeadJobs(JobService jobService, JobRepository jobRepository, final int stopJobAfterSeconds, final Clock clock) {
        this.jobService = jobService;
        this.jobRepository = jobRepository;
        this.stopJobAfterSeconds = stopJobAfterSeconds;
        this.clock = clock;
        LOG.info("Mark old as stopped after '{}' seconds of inactivity", stopJobAfterSeconds);
    }

    @Override
    @Scheduled(fixedRate = STOP_DEAD_JOBS_CLEANUP_INTERVAL)
    public void doCleanUp() {
        OffsetDateTime timeToMarkJobAsStopped = now(clock).minusSeconds(stopJobAfterSeconds);
        LOG.info(format("JobCleanup: Looking for jobs older than %s ", timeToMarkJobAsStopped));
        final List<JobInfo> deadJobs = jobRepository.findRunningWithoutUpdateSince(timeToMarkJobAsStopped);
        deadJobs.forEach(deadJob -> jobService.killJob(deadJob.getJobId()));
    }
}
