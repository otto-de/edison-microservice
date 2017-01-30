package de.otto.edison.jobs.repository.inmem;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.RunningJob;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobLockRepository;
import de.otto.edison.jobs.service.JobMutexGroups;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

public class InMemJobLockRepository implements JobLockRepository {

    private static final Comparator<JobInfo> STARTED_TIME_DESC_COMPARATOR = comparing(JobInfo::getStarted, reverseOrder());

    private final ConcurrentMap<String, JobInfo> jobs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> runningJobs = new ConcurrentHashMap<>();
    private final Set<String> disabledJobTypes = ConcurrentHashMap.newKeySet();
    private final JobMutexGroups mutexGroups;

    public InMemJobLockRepository(final JobMutexGroups mutexGroups) {
        this.mutexGroups = mutexGroups;
    }

    @Override
    public long size() {
        return jobs.size();
    }

    @Override
    public void aquireRunLock(String jobId, String jobType) throws JobBlockedException {
        if (disabledJobTypes.contains(jobType)) {
            throw new JobBlockedException("Disabled");
        }

        synchronized (runningJobs) {
            for (final String mutexJobType : mutexGroups.mutexJobTypesFor(jobType)) {
                if (runningJobs.containsKey(mutexJobType)) {
                    throw new JobBlockedException("Blocked");
                }
            }
            runningJobs.put(jobType, jobId);
        }
    }

    @Override
    public void releaseRunLock(String jobType) {
        runningJobs.remove(jobType);
    }

    @Override
    public List<RunningJob> runningJobs() {
        return this.runningJobs.entrySet().stream()
                .map(entry -> new RunningJob(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }

    @Override
    public void disableJobType(String jobType) {
        disabledJobTypes.add(jobType);
    }

    @Override
    public void enableJobType(String jobType) {
        disabledJobTypes.remove(jobType);
    }

    @Override
    public List<String> disabledJobTypes() {
        return new ArrayList<>(disabledJobTypes);
    }

    @Override
    public void deleteAll() {
        runningJobs.clear();
        disabledJobTypes.clear();
        jobs.clear();
    }

}
