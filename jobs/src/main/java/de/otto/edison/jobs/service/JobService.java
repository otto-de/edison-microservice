package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobType;

import java.net.URI;
import java.util.function.Function;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public interface JobService {

    public URI startAsyncJob(final JobRunnable jobRunnable);

}
