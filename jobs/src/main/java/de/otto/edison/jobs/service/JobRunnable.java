package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobType;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public interface JobRunnable {

    public JobType getJobType();

    public void execute(JobLogger logger);

}
