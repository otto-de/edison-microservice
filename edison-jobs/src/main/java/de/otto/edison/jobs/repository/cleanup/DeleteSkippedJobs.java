package de.otto.edison.jobs.repository.cleanup;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.SKIPPED;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.cleanup.JobCleanupStrategy;

/**
 * A JobCleanupStrategy that is removing all but the newest N skipped jobs.
 * <p>
 *
 * @author Peter Fouquet
 * @since 1.0.0
 */
public class DeleteSkippedJobs implements JobCleanupStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteSkippedJobs.class);
    private static final long KEEP_LAST_JOBS_CLEANUP_INTERVAL = 10L * 60L * 1000L;

    private final int numberOfJobsToKeep;
    private JobRepository jobRepository;

    /**
     * @param numberOfJobsToKeep the number of jobs that are kept
     */
    public DeleteSkippedJobs(final JobRepository jobRepository, final int numberOfJobsToKeep) {
    	this.jobRepository = jobRepository;
        this.numberOfJobsToKeep = numberOfJobsToKeep;
        LOG.info("DeleteSkippedJobs strategy configured with numberOfJobsToKeep='{}'", numberOfJobsToKeep);
    }

    /**
     * Execute the cleanup of the given repository.
     */
    @Scheduled(fixedRate = KEEP_LAST_JOBS_CLEANUP_INTERVAL)
    public void doCleanUp() {
        final List<JobInfo> jobs = jobRepository.findAllJobInfoWithoutMessages();

        findJobsToDelete(jobs)
                .forEach(jobInfo -> jobRepository.removeIfStopped(jobInfo.getJobId()));
    }

    private List<JobInfo> findJobsToDelete(final List<JobInfo> jobs) {
        List<JobInfo> jobsToDelete = new ArrayList<>();
        jobs.stream()
                .sorted(comparing(JobInfo::getStarted, reverseOrder()))
                .collect(groupingBy(JobInfo::getJobType))
                .forEach((jobType, jobExecutions) -> {
                    jobExecutions.stream()
                            .filter(j -> j.isStopped() && Objects.equals(j.getStatus(), SKIPPED))
                            .skip(numberOfJobsToKeep)
                            .forEach(jobsToDelete::add);
                });
        return jobsToDelete;
    }
}
