package de.otto.edison.jobs.monitor;

import de.otto.edison.jobs.domain.JobInfo;

/**
 * @author Guido Steinacker
 * @since 16.07.15
 */
public interface JobMonitor {
    public void update(JobInfo jobInfo);
}
