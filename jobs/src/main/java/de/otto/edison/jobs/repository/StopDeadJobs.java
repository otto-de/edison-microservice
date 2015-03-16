package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class StopDeadJobs implements JobCleanupStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(StopDeadJobs.class);

    private final int stopJobAfterSeconds;

    public StopDeadJobs(final int stopJobAfterSeconds) {
        this.stopJobAfterSeconds = stopJobAfterSeconds;
        LOG.info(format("Mark old as stopped after %s seconds of inactivity.", stopJobAfterSeconds));
    }

    @Override
    public void doCleanUp(JobRepository repository) {
        OffsetDateTime timeToMarkJobAsStopped = OffsetDateTime.now().minusSeconds(stopJobAfterSeconds);
        List<JobInfo> deadJobs = repository.findAll()
                .stream()
                .filter(job -> (!job.getStopped().isPresent() && job.getLastUpdated().isBefore(timeToMarkJobAsStopped)))
                .collect(Collectors.toList());

        for (JobInfo deadJob : deadJobs) {
            JobInfo jobInfo = JobInfoBuilder.copyOf(deadJob).withStopped(deadJob.getLastUpdated()).build();
            repository.createOrUpdate(jobInfo);
        }

    }
}
