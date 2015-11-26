package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository {

    List<JobInfo> findLatest(int maxCount);

    Optional<JobInfo> findOne(URI uri);

    List<JobInfo> findLatestBy(String type, int maxCount);

    List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset);

    List<JobInfo> findAll();

    List<JobInfo> findByType(String jobType);

    Optional<JobInfo> findRunningJobByType(String jobType);

    JobInfo createOrUpdate(JobInfo job);

    void removeIfStopped(URI uri);

    long size();

    JobInfo.JobStatus findStatus(URI jobUri);
}
