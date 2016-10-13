package de.otto.edison.jobs.repository.inmem;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.RunningJobs;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobRepository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.synchronizedSet;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class InMemJobRepository implements JobRepository {

    private static final Comparator<JobInfo> STARTED_TIME_DESC_COMPARATOR = comparing(JobInfo::getStarted, reverseOrder());

    private final ConcurrentMap<String, JobInfo> jobs = new ConcurrentHashMap<>();
    private final HashMap<String, String> runningJobs = new HashMap<>();
    private final Set<String> disabledJobTypes = synchronizedSet(new HashSet<>());

    @Override
    public List<JobInfo> findLatest(int maxCount) {
        return new ArrayList<>(jobs.values()
                .stream()
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .limit(maxCount)
                .collect(toList())
        );
    }

    @Override
    public List<JobInfo> findLatestJobsDistinct() {
        Set<String> typeSet = new HashSet<>();

        return jobs.values()
                .stream()
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .filter(j -> nonNull(j.getJobType()))
                .filter(j -> typeSet.add(j.getJobType()))
                .collect(toList());
    }

    @Override
    public Optional<JobInfo> findOne(final String uri) {
        return ofNullable(jobs.get(uri));
    }

    @Override
    public List<JobInfo> findLatestBy(String type, int maxCount) {
        return jobs.values()
                .stream()
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .filter(jobInfo -> jobInfo.getJobType().equals(type))
                .limit(maxCount)
                .collect(toList());
    }


    @Override
    public List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset) {
        return jobs.values().stream()
                .filter(jobInfo -> !jobInfo.isStopped() && jobInfo.getLastUpdated().isBefore(timeOffset))
                .collect(toList());
    }

    @Override
    public List<JobInfo> findAll() {
        return jobs.values().stream()
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .collect(toList());
    }

    @Override
    public List<JobInfo> findByType(String jobType) {
        return jobs.values().stream()
                .filter(jobInfo -> jobInfo.getJobType().equals(jobType))
                .collect(toList());
    }


    @Override
    public JobInfo createOrUpdate(final JobInfo job) {
        jobs.put(job.getJobId(), job);
        return job;
    }

    @Override
    public void removeIfStopped(final String id) {
        final JobInfo jobInfo = jobs.get(id);
        if (jobInfo != null && jobInfo.isStopped()) {
            jobs.remove(id);
        }
    }

    @Override
    public long size() {
        return jobs.size();
    }

    @Override
    public JobStatus findStatus(String jobId) {
        return jobs.get(jobId).getStatus();
    }

    @Override
    public void appendMessage(String jobId, JobMessage jobMessage) {
        JobInfo jobInfo = jobs.get(jobId);
        jobs.replace(jobId, jobInfo.copy().addMessage(jobMessage).build());
    }

    @Override
    public void setJobStatus(String jobId, JobStatus jobStatus) {
        JobInfo jobInfo = jobs.get(jobId);
        jobs.replace(jobId, jobInfo.copy().setStatus(jobStatus).build());
    }

    @Override
    public void setLastUpdate(String jobId, OffsetDateTime lastUpdate) {
        JobInfo jobInfo = jobs.get(jobId);
        jobs.replace(jobId, jobInfo.copy().setLastUpdated(lastUpdate).build());
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
        synchronized (runningJobs) {
            runningJobs.remove(jobType);
        }
    }

    @Override
    public RunningJobs runningJobsDocument() {
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
	public List<JobInfo> findAllJobInfoWithoutMessages() {
        return jobs.values().stream()
	                .sorted(STARTED_TIME_DESC_COMPARATOR)
	                .map(job->job.copy().setMessages(emptyList()).build())
	                .collect(toList());
	}


}
