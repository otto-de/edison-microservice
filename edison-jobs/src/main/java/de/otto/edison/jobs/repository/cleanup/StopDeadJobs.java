package de.otto.edison.jobs.repository.cleanup;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.time.OffsetDateTime.now;

public class StopDeadJobs implements JobCleanupStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(StopDeadJobs.class);
    private static final long STOP_DEAD_JOBS_CLEANUP_INTERVAL = 10L * 60L * 1000L;

    private final int stopJobAfterSeconds;
    private final Clock clock;
    private JobRepository jobRepository;

    public StopDeadJobs(final int stopJobAfterSeconds, final Clock clock) {
        this.stopJobAfterSeconds = stopJobAfterSeconds;
        this.clock = clock;
        LOG.info("Mark old as stopped after '{}' seconds of inactivity", stopJobAfterSeconds);
    }

    @Autowired
    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Override
    @Scheduled(fixedRate = STOP_DEAD_JOBS_CLEANUP_INTERVAL)
    public void doCleanUp() {
        OffsetDateTime now = now(clock);
        OffsetDateTime timeToMarkJobAsStopped = now.minusSeconds(stopJobAfterSeconds);
        LOG.info(format("JobCleanup: Looking for jobs older than %s ", timeToMarkJobAsStopped));
        final List<JobInfo> deadJobs = jobRepository.findRunningWithoutUpdateSince(timeToMarkJobAsStopped);
        deadJobs.forEach((j) -> {
            // TODO send dead event instead of updating repository directly!
            j.dead();
            jobRepository.createOrUpdate(j);
        });
    }
}
