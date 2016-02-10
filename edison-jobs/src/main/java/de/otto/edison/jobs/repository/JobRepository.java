package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository {

    Optional<JobInfo> findOne(URI uri);

    List<JobInfo> findLatest(int maxCount);

    List<JobInfo> findLatestBy(String type, int maxCount);

    List<JobInfo> findLatestFinishedBy(String type, JobStatus status, int maxCount);

    List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset);

    List<JobInfo> findAll();

    List<JobInfo> findByType(String jobType);

    Optional<JobInfo> findRunningJobByType(String jobType);

    JobInfo createOrUpdate(JobInfo job);

    void removeIfStopped(URI uri);

    long size();

    JobInfo.JobStatus findStatus(URI jobUri);
}
