package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface JobRepository {

    public Optional<JobInfo> findBy(final URI uri);

    public void createOrUpdate(final JobInfo job);

    public List<JobInfo> findAll();

    public int size();

    void deleteOldest(Optional<JobType> jobType);
}
