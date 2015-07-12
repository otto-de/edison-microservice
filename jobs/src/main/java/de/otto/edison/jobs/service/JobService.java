package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public interface JobService {

    public URI startAsyncJob(String jobType);

    public URI startAsyncJob(JobRunnable jobRunnable);

    public List<JobInfo> findJobs(Optional<String> type, int count);

    public Optional<JobInfo> findJob(URI uri);

    public void deleteJobs(Optional<String> type);
}
