package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

/**
 * A JobCleanupStrategy that is removing all but the newest N jobs of an optional JobType.
 * <p>
 * Concurrent executions of the strategy may have the effect, that more JobInfos than expected are deleted! This
 * may happen, if multiple instances of your application are running in parallel.
 *
 * @author Guido Steinacker
 * @since 26.02.15
 */
public class KeepLastJobs implements JobCleanupStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(KeepLastJobs.class);

    private final int numberOfJobsToKeep;
    private final Optional<JobType> jobType;

    /**
     * @param numberOfJobsToKeep the number of jobs that are kept
     * @param jobType            the optional type of the jobs
     */
    public KeepLastJobs(final int numberOfJobsToKeep, final Optional<JobType> jobType) {
        this.numberOfJobsToKeep = numberOfJobsToKeep;
        this.jobType = jobType;
        LOG.info("KeepLastJobs strategy configured with numberOfJobsToKeep=" + numberOfJobsToKeep + ", jobType=" + jobType.toString());
    }

    /**
     * Execute the cleanup of the given repository.
     *
     * @param repository the repo to clean up.
     */
    @Override
    public void doCleanUp(final JobRepository repository) {
        final List<JobInfo> jobs = jobType.isPresent()
                ? repository.findBy(jobType.get(), comparing(JobInfo::getStarted, naturalOrder()))
                : repository.findAll(comparing(JobInfo::getStarted, naturalOrder()));

        if (jobs.size() > numberOfJobsToKeep) {
            jobsToDelete(jobs)
                    .forEach(jobInfo -> repository.removeIfStopped(jobInfo.getJobUri()));
        }
    }

    private List<JobInfo> jobsToDelete(List<JobInfo> jobs){
        int numberOfJobsToDelete = jobs.size() - numberOfJobsToKeep;
        Optional<JobInfo> lastOKJob = findLastOKJob(jobs);
        
        return jobs
                .stream()
                .filter(JobInfo::isStopped)
                .filter(j -> !lastOKJob.isPresent() || j != lastOKJob.get() )
                .limit(numberOfJobsToDelete)
                .collect(toList());
    }

    private Optional<JobInfo> findLastOKJob(List<JobInfo> jobs) {
        return jobs
                .stream()
                .filter(JobInfo::isStopped)
                .filter(j -> JobInfo.JobStatus.OK.equals(j.getStatus()))
                .sorted(Comparator.comparing(JobInfo::getStarted, reverseOrder()))
                .findFirst();
    }

}
