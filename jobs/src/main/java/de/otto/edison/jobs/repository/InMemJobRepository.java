package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Comparator.comparing;
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
        values.sort(comparing(JobInfo::getStarted, reverseOrder()));
        return values;
    }

}
