package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public interface JobRepository {

    public List<JobInfo> findAll(Comparator<JobInfo> comparator);

    public Optional<JobInfo> findBy(final URI uri);

    public List<JobInfo> findBy(final JobType type);

    public void createOrUpdate(final JobInfo job);

    public Optional<JobInfo> deleteOldest(Optional<JobType> jobType);

    public int size();
}
