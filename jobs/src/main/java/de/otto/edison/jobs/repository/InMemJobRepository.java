package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.Optional.ofNullable;

public class InMemJobRepository implements JobRepository {

    private final ConcurrentMap<URI, JobInfo> jobs = new ConcurrentHashMap<>();

    @Override
    public Optional<JobInfo> findBy(URI uri) {
        return ofNullable(jobs.get(uri));
    }

    @Override
    public void createOrUpdate(final JobInfo job) {
        jobs.put(job.getJobUri(), job);
    }

    @Override
    public List<JobInfo> findAll() {
        final List<JobInfo> values = new ArrayList<>(jobs.values());
        values.sort(youngestJobFirst());
        return values;
    }

    private Comparator<JobInfo> youngestJobFirst() {
        return comparing(JobInfo::getStarted, reverseOrder());
    }

    @Override
    public int size() {
        return jobs.size();
    }

    @Override
    public void deleteOldest(final Optional<JobType> jobType) {
        jobs.values().stream()
                .sorted(oldestJobFirst())
                .filter(jobInfoMatchesTo(jobType))
                .findFirst()
                .ifPresent(
                        jobInfo -> jobs.remove(jobInfo.getJobUri())
                );
    }

    private Comparator<JobInfo> oldestJobFirst() {
        return comparing(JobInfo::getStarted, naturalOrder());
    }

    private Predicate<JobInfo> jobInfoMatchesTo(Optional<JobType> jobType) {
        if (jobType.isPresent()) {
            return jobInfo -> (jobInfo.getJobType().equals(jobType.get()) && jobInfo.getStopped().isPresent());
        } else {
            return jobInfo -> jobInfo.getStopped().isPresent();
        }
    }

}
