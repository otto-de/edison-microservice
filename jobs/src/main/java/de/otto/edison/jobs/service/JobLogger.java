package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;

/**
 * @author Guido Steinacker
 * @since 23.02.15
 */
public interface JobLogger {

    void log(JobMessage message);

}
