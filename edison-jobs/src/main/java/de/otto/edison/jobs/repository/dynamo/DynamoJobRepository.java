package de.otto.edison.jobs.repository.dynamo;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.repository.JobRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public class DynamoJobRepository implements JobRepository {
    @Override
    public Optional<JobInfo> findOne(String jobId) {
        return Optional.empty();
    }

    @Override
    public List<JobInfo> findLatest(int maxCount) {
        return null;
    }

    @Override
    public List<JobInfo> findLatestJobsDistinct() {
        return null;
    }

    @Override
    public List<JobInfo> findLatestBy(String type, int maxCount) {
        return null;
    }

    @Override
    public List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset) {
        return null;
    }

    @Override
    public List<JobInfo> findAll() {
        return null;
    }

    @Override
    public List<JobInfo> findAllJobInfoWithoutMessages() {
        return null;
    }

    @Override
    public List<JobInfo> findByType(String jobType) {
        return null;
    }

    @Override
    public JobInfo createOrUpdate(JobInfo job) {
        return null;
    }

    @Override
    public void removeIfStopped(String jobId) {

    }

    @Override
    public JobInfo.JobStatus findStatus(String jobId) {
        return null;
    }

    @Override
    public void appendMessage(String jobId, JobMessage jobMessage) {

    }

    @Override
    public void setJobStatus(String jobId, JobInfo.JobStatus jobStatus) {

    }

    @Override
    public void setLastUpdate(String jobId, OffsetDateTime lastUpdate) {

    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public void deleteAll() {

    }
}
