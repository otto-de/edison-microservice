package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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
    public Optional<JobInfo> findBy(final URI uri) {
        return ofNullable(jobs.get(uri));
    }

    @Override
    public List<JobInfo> findLatestBy(String type, int maxCount) {
        return jobs.values()
                .stream()
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .filter(jobInfo -> jobInfo.getJobType() == type)
                .collect(toList());
    }

    @Override
    public List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset) {
        return jobs.values().stream()
                .filter(jobInfo -> jobInfo.getLastUpdated().isBefore(timeOffset))
                .collect(toList());
    }

    @Override
    public List<JobInfo> findAll() {
        return jobs.values().stream()
                .sorted(STARTED_TIME_DESC_COMPARATOR)
                .collect(toList());
    }

    @Override
    public JobInfo findRunningJobByType(String jobType) {
        final List<JobInfo> runningJobsOfType = jobs.values().stream()
                .filter(job -> job.getJobType().equals(jobType) && !job.getStopped().isPresent())
                .collect(Collectors.toList());
        return runningJobsOfType.isEmpty() ? null : runningJobsOfType.get(0);
    }

    @Override
    public void createOrUpdate(final JobInfo job) {
        jobs.put(job.getJobUri(), job);
    }

    @Override
    public void removeIfStopped(final URI uri) {
        final JobInfo jobInfo = jobs.get(uri);
        if (jobInfo != null && jobInfo.isStopped()) {
            jobs.remove(uri);
        }
    }

    @Override
    public int size() {
        return jobs.size();
    }

}
