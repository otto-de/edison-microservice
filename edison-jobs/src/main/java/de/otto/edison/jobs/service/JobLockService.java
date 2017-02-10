package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.DisabledJob;
import de.otto.edison.jobs.domain.RunningJob;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobStateRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A service used to manage locking of jobs.
 */
@Service
public class JobLockService {

    private static final Logger LOG = getLogger(JobLockService.class);

    private static final String KEY_DISABLED = "_e_disabled";
    private static final String KEY_RUNNING = "_e_running";

    private final JobStateRepository stateRepository;
    private final JobMutexGroups mutexGroups;

    @Autowired
    public JobLockService(final JobStateRepository stateRepository,
                          final JobMutexGroups mutexGroups) {
        this.stateRepository = stateRepository;
        this.mutexGroups = mutexGroups;
    }

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
    public void aquireRunLock(final String jobId, final String jobType) throws JobBlockedException {

        // check for disabled lock:
        if (disabledJobTypes().stream().anyMatch(disabled->jobType.equals(disabled.jobType))) { // TODO nur beim triggern prüfen??
            throw new JobBlockedException(format("Job '%s' is currently disabled", jobType));
        }

        // aquire lock:
        if (stateRepository.createValue(jobType, KEY_RUNNING, jobId)) {

            // check for mutually exclusive running jobs:
            mutexGroups.mutexJobTypesFor(jobType)
                    .stream()
                    .filter(mutexJobType -> stateRepository.getValue(mutexJobType, KEY_RUNNING) != null)
                    .findAny()
                    .ifPresent(running -> {
                        releaseRunLock(jobType);
                        throw new JobBlockedException(format("Job '%s' blocked by currently running job '%s'", jobType, running));
                    });
        } else {
            throw new JobBlockedException(format("Job '%s' is already running", jobType));
        }
    }

    /**
     * Clears the job running mark of the jobType. Does nothing if not mark exists.
     *
     * @param jobType the job type
     */
    public void releaseRunLock(final String jobType) {
        stateRepository.setValue(jobType, KEY_RUNNING, null);
    }

    /**
     * @return All Running Jobs as specified by the markJobAsRunningIfPossible method.
     */
    public Set<RunningJob> runningJobs() {
        final Set<RunningJob> runningJobs = new HashSet<>();
        stateRepository.findAllJobTypes()
                .forEach(jobType -> {
                    final String jobId = stateRepository.getValue(jobType, KEY_RUNNING);
                    if (jobId != null) {
                        runningJobs.add(new RunningJob(jobId, jobType));
                    }
                });
        return runningJobs;
    }

    /**
     * Disables a job type, i.e. prevents it from being started
     *
     * @param disabledJob the disabled job type with an optional comment
     */
    public void disableJobType(final DisabledJob disabledJob) {
        stateRepository.setValue(disabledJob.jobType, KEY_DISABLED, disabledJob.comment);
    }

    /**
     * Reenables a job type that was disabled
     *
     * @param jobType the enabled job type
     */
    public void enableJobType(final String jobType) {
        stateRepository.setValue(jobType, KEY_DISABLED, null);
    }

    /**
     * @return a list of all job types that are currently disabled
     */
    public Set<DisabledJob> disabledJobTypes() {
        return stateRepository.findAllJobTypes()
                .stream()
                .map((jobType) -> {
                    final String comment = stateRepository.getValue(jobType, KEY_DISABLED);
                    if (comment != null) {
                        return new DisabledJob(jobType, comment);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(toSet());
    }

}
