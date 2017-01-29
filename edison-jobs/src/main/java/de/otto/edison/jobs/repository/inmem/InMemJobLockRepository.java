package de.otto.edison.jobs.repository.inmem;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.RunningJobs;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobLockRepository;
import de.otto.edison.jobs.repository.JobRepository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class InMemJobLockRepository implements JobLockRepository {

    private static final Comparator<JobInfo> STARTED_TIME_DESC_COMPARATOR = comparing(JobInfo::getStarted, reverseOrder());

    private final ConcurrentMap<String, JobInfo> jobs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> runningJobs = new ConcurrentHashMap<>();
    private final Set<String> disabledJobTypes = ConcurrentHashMap.newKeySet();

    @Override
    public long size() {
        return jobs.size();
    }

    @Override
    public void markJobAsRunningIfPossible(JobInfo job, Set<String> blockingJobs) throws JobBlockedException {
        if (disabledJobTypes.contains(job.getJobType())) {
            throw new JobBlockedException("Disabled");
        }

        synchronized (runningJobs) {
            for (String mutexJobType : blockingJobs) {
                if (runningJobs.containsKey(mutexJobType)) {
                    throw new JobBlockedException("Blocked");
                }
            }
            runningJobs.put(job.getJobType(), job.getJobId());
        }
    }

    @Override
    public void clearRunningMark(String jobType) {
        runningJobs.remove(jobType);
    }

    @Override
    public RunningJobs runningJobs() {
        List<RunningJobs.RunningJob> runningJobs = this.runningJobs.entrySet().stream()
                .map(entry -> new RunningJobs.RunningJob(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());

        return new RunningJobs(runningJobs);
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
    public List<String> findDisabledJobTypes() {
        return new ArrayList(disabledJobTypes);
    }

    @Override
    public void deleteAll() {
        runningJobs.clear();
        disabledJobTypes.clear();
        jobs.clear();
    }


}
