package de.otto.edison.jobs.repository.cleanup;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.groupingBy;

/**
 * A JobCleanupStrategy that is removing all but the newest N jobs of each type.
 * <p>
 * Concurrent executions of the strategy may have the effect, that more JobInfos than expected are deleted! This
 * may happen, if multiple instances of your application are running in parallel.
 *
 * @author Guido Steinacker
 * @since 26.02.15
 */
public class KeepLastJobs implements JobCleanupStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(KeepLastJobs.class);
    private static final long KEEP_LAST_JOBS_CLEANUP_INTERVAL = 10L * 60L * 1000L;

    private final int numberOfJobsToKeep;
    private JobRepository jobRepository;

    /**
     * @param numberOfJobsToKeep the number of jobs that are kept
     */
    public KeepLastJobs(final int numberOfJobsToKeep) {
        this.numberOfJobsToKeep = numberOfJobsToKeep;
        LOG.info("KeepLastJobs strategy configured with numberOfJobsToKeep='{}'", numberOfJobsToKeep);
    }

    @Autowired
    public void setJobRepository(final JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * Execute the cleanup of the given repository.
     */
    @Scheduled(fixedRate = KEEP_LAST_JOBS_CLEANUP_INTERVAL)
    public void doCleanUp() {
        final List<JobInfo> jobs = jobRepository.findAll();

        findJobsToDelete(jobs)
                .forEach(jobInfo -> jobRepository.removeIfStopped(jobInfo.getJobId()));
    }

    private List<JobInfo> findJobsToDelete(List<JobInfo> jobs) {
        List<JobInfo> jobsToDelete = new ArrayList<>();
        jobs.stream()
                .sorted(comparing(JobInfo::getStarted, reverseOrder()))
                .collect(groupingBy(JobInfo::getJobType))
                .forEach((jobType, jobExecutions) -> {
                    Optional<JobInfo> lastOkExecution = jobExecutions.stream()
                            .filter(j -> j.isStopped() && j.getStatus() == OK)
                            .findFirst();
                    jobExecutions.stream()
                            .filter(JobInfo::isStopped)
                            .skip(numberOfJobsToKeep)
                            .filter(j -> !(lastOkExecution.isPresent() && lastOkExecution.get().equals(j)))
                            .forEach(jobsToDelete::add);
                });
        return jobsToDelete;
    }

}
