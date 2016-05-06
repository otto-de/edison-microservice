package de.otto.edison.jobs.repository.inmem;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.repository.JobRepository;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class InMemJobRepository implements JobRepository {

    private static final Comparator<JobInfo> STARTED_TIME_DESC_COMPARATOR = comparing(JobInfo::getStarted, reverseOrder());

    private final ConcurrentMap<URI, JobInfo> jobs = new ConcurrentHashMap<>();

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
    public Optional<JobInfo> findOne(final URI uri) {
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
    public List<JobInfo> findLatestFinishedBy(String type, JobStatus status, int maxCount) {
        return jobs.values()
                .stream()
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .filter(jobInfo ->
                        jobInfo.getJobType().equals(type)
                                && jobInfo.getStatus().equals(status)
                                && jobInfo.isStopped()
                )
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
    public Optional<JobInfo> findRunningJobByType(String jobType) {
        final List<JobInfo> runningJobsOfType = jobs.values().stream()
                .filter(job -> job.getJobType().equals(jobType) && !job.getStopped().isPresent())
                .collect(toList());
        return Optional.ofNullable(runningJobsOfType.isEmpty() ? null : runningJobsOfType.get(0));
    }

    @Override
    public JobInfo createOrUpdate(final JobInfo job) {
        jobs.put(job.getJobUri(), job);
        return job;
    }

    @Override
    public void removeIfStopped(final URI uri) {
        final JobInfo jobInfo = jobs.get(uri);
        if (jobInfo != null && jobInfo.isStopped()) {
            jobs.remove(uri);
        }
    }

    @Override
    public long size() {
        return jobs.size();
    }

    @Override
    public JobStatus findStatus(URI jobUri) {
        return jobs.get(jobUri).getStatus();
    }

    @Override
    public void appendMessage(URI jobUri, JobMessage jobMessage) {
        JobInfo jobInfo = jobs.get(jobUri);
        jobInfo.getMessages().add(jobMessage);
    }


}
