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

    Optional<URI> startAsyncJob(String jobType);

    Optional<URI> startAsyncJob(JobRunnable jobRunnable);

    List<JobInfo> findJobs(Optional<String> type, int count);

    Optional<JobInfo> findJob(URI uri);

    void deleteJobs(Optional<String> type);
}
