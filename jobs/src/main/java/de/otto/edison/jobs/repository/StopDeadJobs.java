package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.DEAD;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.domain.Level.INFO;
import static java.lang.String.format;
import static java.time.OffsetDateTime.now;

public class StopDeadJobs implements JobCleanupStrategy {

    public static final String JOB_DEAD_MESSAGE = "Job didn't receive updates for a while, considering it dead";
    private static final Logger LOG = LoggerFactory.getLogger(StopDeadJobs.class);
    private static final long STOP_DEAD_JOBS_CLEANUP_INTERVAL = 60L * 1000L;

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

    @Scheduled(fixedRate = STOP_DEAD_JOBS_CLEANUP_INTERVAL)
    public void doCleanUp() {
        OffsetDateTime now = now(clock);
        OffsetDateTime timeToMarkJobAsStopped = now.minusSeconds(stopJobAfterSeconds);
        LOG.info(format("JobCleanup: Looking for jobs older than %s ", timeToMarkJobAsStopped));
        List<JobInfo> deadJobs = jobRepository.findRunningWithoutUpdateSince(timeToMarkJobAsStopped);

        for (JobInfo deadJob : deadJobs) {
            LOG.info("Marking job as dead: {}", deadJob);
            JobInfo jobInfo = JobInfoBuilder
                    .copyOf(deadJob)
                    .withStopped(now)
                    .withLastUpdated(now)
                    .withStatus(DEAD)
                    .addMessage(jobMessage(INFO, JOB_DEAD_MESSAGE))
                    .build();
            jobRepository.createOrUpdate(jobInfo);
        }
    }
}
