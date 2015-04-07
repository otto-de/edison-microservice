package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfoBuilder;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.service.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.DEAD;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.domain.Level.INFO;
import static java.lang.String.format;

public class StopDeadJobs implements JobCleanupStrategy {

    public static final String JOB_DEAD_MESSAGE = "Job didn't receive updates for a while, considering it dead";
    private static final Logger LOG = LoggerFactory.getLogger(StopDeadJobs.class);

    private final int stopJobAfterSeconds;
    private final Clock clock;

    public StopDeadJobs(final int stopJobAfterSeconds, Clock clock) {
        this.stopJobAfterSeconds = stopJobAfterSeconds;
        this.clock = clock;
        LOG.info(format("Mark old as stopped after %s seconds of inactivity.", stopJobAfterSeconds));
    }

    @Override
    public void doCleanUp(JobRepository repository) {
        OffsetDateTime now = clock.now();
        OffsetDateTime timeToMarkJobAsStopped = now.minusSeconds(stopJobAfterSeconds);
        List<JobInfo> deadJobs = repository.findAll()
                .stream()
                .filter(job -> (!job.isStopped() && job.getLastUpdated().isBefore(timeToMarkJobAsStopped)))
                .collect(Collectors.toList());

        for (JobInfo deadJob : deadJobs) {
            LOG.info("Marking job as dead: {}",deadJob);
            JobInfo jobInfo = JobInfoBuilder
                    .copyOf(deadJob)
                    .withStopped(now)
                    .withLastUpdated(now)
                    .withStatus(DEAD)
                    .addMessage(jobMessage(INFO, JOB_DEAD_MESSAGE))
                    .build();
            repository.createOrUpdate(jobInfo);
        }

    }
}
