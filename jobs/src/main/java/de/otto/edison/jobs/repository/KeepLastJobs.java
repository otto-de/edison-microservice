package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
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
        final List<JobInfo> stoppedJobs = jobs
                .stream()
                .filter(jobInfo -> jobInfo.getStopped().isPresent())
                .collect(toList());
        for (int i=0, n=stoppedJobs.size()-numberOfJobsToKeep; i<n; ++i) {
            repository.removeIfStopped(jobs.get(i).getJobUri());
        }
    }

}
