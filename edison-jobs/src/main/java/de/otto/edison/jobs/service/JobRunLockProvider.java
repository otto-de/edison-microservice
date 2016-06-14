package de.otto.edison.jobs.service;

import java.util.Set;

public interface JobRunLockProvider {

    /**
     * Returns true, if all desired locks can be obtained.
     * This method has to always aquire all or no locks.
     */
    boolean acquireRunLocksForJobTypes(Set<String> jobTypes);

    /**
     * Releases all given locks.
     * If the lock is already released, no exception is thrown.
     */
    void releaseRunLocksForJobTypes(Set<String> mutexJobTypes);
}
