package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static java.util.Comparator.comparing;
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
    private final Optional<String> jobType;

    /**
     * @param numberOfJobsToKeep the number of jobs that are kept
     * @param jobType            the optional type of the jobs
     */
    public KeepLastJobs(final int numberOfJobsToKeep, final Optional<String> jobType) {
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
                ? repository.findByType(jobType.get())
                : repository.findAll();

        if (jobs.size() > numberOfJobsToKeep) {
            findJobsToDelete(jobs)
                    .forEach(jobInfo -> repository.removeIfStopped(jobInfo.getJobUri()));
        }
    }

    private List<JobInfo> findJobsToDelete(List<JobInfo> jobs) {
        int numberOfJobsToDelete = jobs.size() - numberOfJobsToKeep;
        List<JobInfo> lastOKJobs = findLastOKJobs(jobs);

        return jobs
                .stream()
                .filter(JobInfo::isStopped)
                .filter(j -> !lastOKJobs.contains(j))
                .sorted(comparing(JobInfo::getStarted))
                .limit(numberOfJobsToDelete)
                .collect(toList());
    }

    private List<JobInfo> findLastOKJobs(List<JobInfo> jobs) {
        Map<String, List<JobInfo>> jobsGroupedByType = jobs.stream().collect(Collectors.groupingBy(o -> o.getJobType()));

        return jobsGroupedByType.entrySet().stream().map(entry -> {
            return entry.getValue().stream()
                    .filter(j -> j.isStopped() && j.getStatus() == OK)
                    .sorted(comparing(JobInfo::getStarted, reverseOrder()))
                    .findFirst();
        }).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }
}
