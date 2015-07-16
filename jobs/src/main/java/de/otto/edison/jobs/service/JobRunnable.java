package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public interface JobRunnable {

    String getJobType();

    void execute(JobInfo jobInfo);

}
