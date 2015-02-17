package de.otto.µservice.jobs.service;

import java.net.URI;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public interface JobService {

    public URI startAsyncJob(JobRunnable jobRunnable);

}
