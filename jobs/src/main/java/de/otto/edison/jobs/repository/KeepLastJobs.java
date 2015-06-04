package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * @param numberOfJobsToKeep the number of jobs that are kept
     */
    public KeepLastJobs(final int numberOfJobsToKeep) {
        this.numberOfJobsToKeep = numberOfJobsToKeep;
        LOG.info("KeepLastJobs strategy configured with numberOfJobsToKeep=" + numberOfJobsToKeep);
    }

    /**
     * Execute the cleanup of the given repository.
     *
     * @param repository the repo to clean up.
     */
    @Override
    public void doCleanUp(final JobRepository repository) {
        final List<JobInfo> jobs = repository.findAll();

        if (jobs.size() > numberOfJobsToKeep) {
            jobsToDelete(jobs)
                    .forEach(jobInfo -> repository.removeIfStopped(jobInfo.getJobUri()));
        }
    }

    private List<JobInfo> jobsToDelete(List<JobInfo> jobs) {
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
