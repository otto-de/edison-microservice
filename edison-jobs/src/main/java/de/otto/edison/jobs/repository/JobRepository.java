package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.eventbus.events.MessageEvent;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository {

    Optional<JobInfo> findOne(String jobId);

    List<JobInfo> findLatest(int maxCount);

    List<JobInfo> findLatestBy(String type, int maxCount);

    List<JobInfo> findLatestFinishedBy(String type, JobStatus status, int maxCount);

    List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset);

    List<JobInfo> findAll();

    List<JobInfo> findByType(String jobType);

    Optional<JobInfo> findRunningJobByType(String jobType);

    JobInfo createOrUpdate(JobInfo job);

    void removeIfStopped(String jobId);

    long size();

    JobInfo.JobStatus findStatus(String jobId);

    void appendMessage(String jobId, JobMessage jobMessage);
}
