package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class InMemJobRepository implements JobRepository {

    private final ConcurrentMap<URI, JobInfo> jobs = new ConcurrentHashMap<>();

    @Override
    public List<JobInfo> findAll(final Comparator<JobInfo> comparator) {
        return new ArrayList<>(jobs.values()
                .stream()
                .sorted(comparator)
                .collect(toList())
        );
    }

    @Override
    public Optional<JobInfo> findBy(final URI uri) {
        return ofNullable(jobs.get(uri));
    }

    @Override
    public List<JobInfo> findBy(final JobType type) {
        return new ArrayList<>(jobs.values())
                .stream()
                .sorted(comparing(JobInfo::getStarted, reverseOrder()))
                .filter(jobInfo -> jobInfo.getJobType() == type)
                .collect(toList());
    }

    @Override
    public void createOrUpdate(final JobInfo job) {
        jobs.put(job.getJobUri(), job);
    }

    @Override
    public Optional<JobInfo> deleteOldest(final Optional<JobType> jobType) {
        final Optional<JobInfo> first = jobs.values().stream()
                .sorted(oldestJobFirst())
                .filter(jobInfoMatchesTo(jobType))
                .findFirst();
        first.ifPresent((jobInfo) -> jobs.remove(jobInfo.getJobUri()));
        return first;
    }

    @Override
    public int size() {
        return jobs.size();
    }

    private Comparator<JobInfo> oldestJobFirst() {
        return comparing(JobInfo::getStarted, naturalOrder());
    }

    private Predicate<JobInfo> jobInfoMatchesTo(final Optional<JobType> jobType) {
        if (jobType.isPresent()) {
            return jobInfo -> (jobInfo.getJobType().equals(jobType.get()) && jobInfo.getStopped().isPresent());
        } else {
            return jobInfo -> jobInfo.getStopped().isPresent();
        }
    }

}
