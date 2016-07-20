package de.otto.edison.jobs.repository.cleanup;

import de.otto.edison.jobs.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class StopDeadJobs implements JobCleanupStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(StopDeadJobs.class);
    private static final long STOP_DEAD_JOBS_CLEANUP_INTERVAL = 10L * 60L * 1000L;

    private final int stopJobAfterSeconds;
    private final JobService jobService;

    public StopDeadJobs(final JobService jobService, final int stopJobAfterSeconds) {
        this.jobService = jobService;
        this.stopJobAfterSeconds = stopJobAfterSeconds;
        LOG.info("Mark old as stopped after '{}' seconds of inactivity", stopJobAfterSeconds);
    }

    @Override
    @Scheduled(fixedRate = STOP_DEAD_JOBS_CLEANUP_INTERVAL)
    public void doCleanUp() {
        jobService.killJobsDeadSince(stopJobAfterSeconds);
    }
}
