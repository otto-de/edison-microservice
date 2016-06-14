package de.otto.edison.jobs.service;

import de.otto.edison.annotations.Beta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * This mutex handler is responsible for checking if a job can currently be started.
 * Reasons for not being startable are, either the job is already running or any other job in the same MutexGroup
 * is already running.
 *
 * As a user of edison microservice you can define a set of MutexGroups to make sure, that only one job of
 * a mutex group can run at the same time.
 */
@Service
@Beta
public class JobMutexHandler {

    @Autowired(required = false)
    private Set<JobMutexGroup> mutexGroups;

    @Autowired
    private JobRunLockProvider jobRunLockProvider;

    public JobMutexHandler() {
    }

    public JobMutexHandler(Set<JobMutexGroup> mutexGroups, JobRunLockProvider jobRunLockProvider) {
        this.mutexGroups = mutexGroups;
        this.jobRunLockProvider = jobRunLockProvider;
    }

    @PostConstruct
    private void postConstruct() {
        if (mutexGroups == null) {
            this.mutexGroups = emptySet();
        }
    }

    public boolean isJobStartable(String jobType) {
        final Set<String> mutexJobTypes = mutexJobTypesFor(jobType);
        return jobRunLockProvider.acquireRunLocksForJobTypes(mutexJobTypes);
    }

    public void jobHasStopped(String jobType) {
        final Set<String> mutexJobTypes = mutexJobTypesFor(jobType);
        jobRunLockProvider.releaseRunLocksForJobTypes(mutexJobTypes);
    }

    private Set<String> mutexJobTypesFor(final String jobType) {
        final Set<String> result = new HashSet<>();
        result.add(jobType);
        this.mutexGroups
                .stream()
                .map(JobMutexGroup::getJobTypes)
                .filter(g->g.contains(jobType))
                .forEach(result::addAll);
        return result;
    }
}
