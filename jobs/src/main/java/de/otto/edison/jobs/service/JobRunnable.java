package de.otto.edison.jobs.service;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public interface JobRunnable {

    String getJobType();

    void execute(JobLogger logger);

}
