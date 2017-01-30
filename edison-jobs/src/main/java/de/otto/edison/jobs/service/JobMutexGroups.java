package de.otto.edison.jobs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Component used to determine the mutually exclusive job types for a given type.
 */
@Component
public class JobMutexGroups {

    private final Set<JobMutexGroup> mutexGroups;

    /**
     * Creates a {@code JobMutexGroups} component from autowired {@link JobMutexGroup}s.
     *
     * @param mutexGroups zero or more JobMutexGroup configurations
     */
    @Autowired(required = false)
    public JobMutexGroups(final Set<JobMutexGroup> mutexGroups) {
        this.mutexGroups = mutexGroups != null ? mutexGroups : emptySet();
    }

    /**
     * Returns the set of configured JobMutextGroups.
     *
     * @return set of mutex groups
     */
    public Set<JobMutexGroup> getMutexGroups() {
        return mutexGroups;
    }

    /**
     * Returns the set of mutually exclusive job types for a given type.
     * <p>
     * Because every job type is mutually exclusive with itself, the returned set will at least contain the
     * type provided as parameter.
     * </p>
     * @param jobType the given job type
     * @return set of mutually exclusive job types
     */
    public Set<String> mutexJobTypesFor(final String jobType) {
        final Set<String> result = new HashSet<>();
        result.add(jobType);
        this.mutexGroups
                .stream()
                .map(JobMutexGroup::getJobTypes)
                .filter(g -> g.contains(jobType))
                .forEach(result::addAll);
        return result;
    }


}
