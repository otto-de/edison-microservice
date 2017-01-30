package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.RunningJob;

import java.util.List;

/**
 * A repository that is holding locks for running or disabled jobs.
 *
 * @since 1.0.0
 */
public interface JobLockRepository {

    /**
     * Marks a job as running or throws JobBlockException if it is either disabled, was marked running before or is
     * blocked by some other job from the mutex group. This operation must be implemented atomically on the persistent
     * datastore (i. e. test and set) to make sure a job is never marked as running twice.
     *
     * @param jobId the id of the job
     * @param jobType the type of the job
     * @throws JobBlockedException if at least one of the jobTypes in the jobTypesMutex set is already marked running, or
     * if the job type was disabled.
     */
    void aquireRunLock(String jobId, String jobType) throws JobBlockedException;

    /**
     * Clears the job running mark of the jobType. Does nothing if not mark exists.
     *
     * @param jobType the job type
     */
    void releaseRunLock(String jobType);

    /**
     * @return All Running Jobs as specified by the markJobAsRunningIfPossible method.
     */
    List<RunningJob> runningJobs();

    /**
     * Disables a job type, i.e. prevents it from being started
     *
     * @param jobType the disabled job type
     */
    void disableJobType(String jobType);

    /**
     * Reenables a job type that was disabled
     *
     * @param jobType the enabled job type
     */
    void enableJobType(String jobType);

    /**
     * @return a list of all job types that are currently disabled
     */
    List<String> disabledJobTypes();

    void deleteAll();

    long size();
}
