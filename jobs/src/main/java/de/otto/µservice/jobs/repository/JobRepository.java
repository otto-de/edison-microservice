package de.otto.µservice.jobs.repository;

import de.otto.µservice.jobs.domain.JobInfo;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface JobRepository {

    public Optional<JobInfo> findBy(final URI uri);

    public void createOrUpdate(final JobInfo job);

    public List<JobInfo> findAll();
}
