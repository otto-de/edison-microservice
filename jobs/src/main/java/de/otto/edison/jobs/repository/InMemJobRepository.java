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

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
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
        return findBy(type, comparing(JobInfo::getStarted, reverseOrder()));
    }

    @Override
    public List<JobInfo> findBy(final JobType type, final Comparator<JobInfo> comparator) {
        return new ArrayList<>(jobs.values())
                .stream()
                .sorted(comparator)
                .filter(jobInfo -> jobInfo.getJobType() == type)
                .collect(toList());
    }

    @Override
    public void createOrUpdate(final JobInfo job) {
        jobs.put(job.getJobUri(), job);
    }

    @Override
    public void removeIfStopped(final URI uri) {
        final JobInfo jobInfo = jobs.get(uri);
        if (jobInfo != null && jobInfo.getStopped().isPresent()) {
            jobs.remove(uri);
        }
    }

    @Override
    public int size() {
        return jobs.size();
    }

}
