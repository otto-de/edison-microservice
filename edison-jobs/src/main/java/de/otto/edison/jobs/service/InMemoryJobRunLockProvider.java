package de.otto.edison.jobs.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This default implementation stores the acquired locks in memory.
 */
public class InMemoryJobRunLockProvider implements JobRunLockProvider {

    protected Set<String> locks = new HashSet<>();

    @Override
    public synchronized boolean acquireRunLockForJobType(String jobType) {
        return locks.add(jobType);
    }

    @Override
    public synchronized boolean acquireRunLocksForJobTypes(Set<String> jobTypes) {
        if (Collections.disjoint(locks, jobTypes)) {
            locks.addAll(jobTypes);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void releaseRunLockForJobType(String jobType) {
        locks.remove(jobType);
    }

    @Override
    public synchronized void releaseRunLocksForJobTypes(Set<String> jobTypes) {
        locks.removeAll(jobTypes);
    }
}
