package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.RunningJobs;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface JobRepository {

    Optional<JobInfo> findOne(String jobId);

    List<JobInfo> findLatest(int maxCount);

    List<JobInfo> findLatestJobsDistinct();

    List<JobInfo> findLatestBy(String type, int maxCount);

    List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset);

    List<JobInfo> findAll();

    List<JobInfo> findByType(String jobType);

    JobInfo createOrUpdate(JobInfo job);

    void removeIfStopped(String jobId);

    long size();

    JobInfo.JobStatus findStatus(String jobId);

    void appendMessage(String jobId, JobMessage jobMessage);

    /**
     * Marks a job as running or throws JobBlockException if it was marked running before or is blocked by some other
     * job from the mutex group. This operation must be implemented atomically on the persistent datastore (i. e. test
     * and set)
     *
     * @param job the job to be marked
     * @param jobTypesMutex a list of job types that must not be marked running in order to mark this job.
     *                      The jobType to be marked will be contained in this set.
     * @throws JobBlockedException if at least one of the jobTypes in the jobTypesMutex set is already marked running.
     */
    void markJobAsRunningIfPossible(JobInfo job, Set<String> jobTypesMutex) throws JobBlockedException;

    /**
     * Clears the job running mark of the jobType. Does nothing if not mark exists.
     * @param jobType
     */
    void clearRunningMark(String jobType);

    RunningJobs runningJobsDocument();

}
