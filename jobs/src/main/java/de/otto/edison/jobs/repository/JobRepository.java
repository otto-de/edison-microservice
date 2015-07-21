package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository {

    public List<JobInfo> findLatest(int maxCount);

    public Optional<JobInfo> findOne(URI uri);

    public List<JobInfo> findLatestBy(String type, int maxCount);

    public List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset);

    public List<JobInfo> findAll();

    public List<JobInfo> findByType(String jobType);

    public Optional<JobInfo> findRunningJobByType(String jobType);

    public void createOrUpdate(JobInfo job);

    public void removeIfStopped(URI uri);

    public long size();

}
