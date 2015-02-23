package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobType;

import java.net.URI;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public interface JobService {

    public default URI startAsyncJob(final JobType jobType, final Runnable runnable) {
        return startAsyncJob(new JobRunnable() {
            @Override
            public JobType getJobType() {
                return jobType;
            }

            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    public URI startAsyncJob(final JobRunnable jobRunnable);

}
