package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * A service used to manage jobs in Edison microservices.
 *
 * @author Guido Steinacker
 * @since 15.02.15
 */
public interface JobService {

    /**
     * Starts a job asynchronously in the background.
     *
     * @param jobType the type of the job
     * @return
     */
    Optional<URI> startAsyncJob(String jobType);

    /**
     * Find the latest jobs, optionally restricted to jobs of a specified type.
     *
     * @param type if provided, the last N jobs of the type are returned, otherwise the last jobs of any type.
     * @param count the number of jobs to return.
     * @return a list of JobInfos
     */
    List<JobInfo> findJobs(Optional<String> type, int count);

    Optional<JobInfo> findJob(URI uri);

    void deleteJobs(Optional<String> type);
}
