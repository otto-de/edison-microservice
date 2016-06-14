package de.otto.edison.jobs.service;

import java.util.Set;

public interface JobRunLockProvider {

    boolean acquireRunLockForJobType(String jobType);

    /**
     * Returns true, if all desired locks can be obtained.
     * This method has to always acquires all or no locks.
     * @param jobTypes a list of jobtypes, for which the locks should be acquired
     * @return true, only if all locks could be acquired
     */
    boolean acquireRunLocksForJobTypes(Set<String> jobTypes);

    void releaseRunLockForJobType(String jobType);

    /**
     * Releases all given locks.
     * If the lock is already released, no exception is thrown.
     * @param jobTypes a list of jobtypes, for which the locks should be released
     */
    void releaseRunLocksForJobTypes(Set<String> jobTypes);
}
