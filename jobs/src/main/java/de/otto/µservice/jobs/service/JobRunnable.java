package de.otto.µservice.jobs.service;

import de.otto.µservice.jobs.domain.JobType;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public interface JobRunnable extends Runnable {

    public JobType getJobType();

}
