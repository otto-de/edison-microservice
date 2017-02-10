package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository {

    Optional<JobInfo> findOne(String jobId);

    List<JobInfo> findLatest(int maxCount);

    List<JobInfo> findLatestJobsDistinct();

    List<JobInfo> findLatestBy(String type, int maxCount);

    List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset);

    List<JobInfo> findAll();

    /**
     * @return all jobs without loading the job messages
     */
    List<JobInfo> findAllJobInfoWithoutMessages();

    List<JobInfo> findByType(String jobType);

    JobInfo createOrUpdate(JobInfo job);

    void removeIfStopped(String jobId);

    JobInfo.JobStatus findStatus(String jobId);

    void appendMessage(String jobId, JobMessage jobMessage);

    void setJobStatus(String jobId, JobInfo.JobStatus jobStatus);

    void setLastUpdate(String jobId, OffsetDateTime lastUpdate);

    long size();

    void deleteAll();
}
