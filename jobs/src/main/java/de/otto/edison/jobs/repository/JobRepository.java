package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;

public interface JobRepository {

    public default List<JobInfo> findAll() {
        return findAll(comparing(JobInfo::getStarted, reverseOrder()));
    }

    public List<JobInfo> findAll(Comparator<JobInfo> comparator);

    public Optional<JobInfo> findBy(URI uri);

    public default List<JobInfo> findBy(JobType type) {
        return findBy(type, comparing(JobInfo::getStarted, reverseOrder()));
    }

    public List<JobInfo> findBy(JobType type, Comparator<JobInfo> comparator);

    public void createOrUpdate(JobInfo job);

    public void removeIfStopped(URI uri);

    public int size();
}
