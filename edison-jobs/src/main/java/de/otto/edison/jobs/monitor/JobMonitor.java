package de.otto.edison.jobs.monitor;

import de.otto.edison.jobs.domain.JobInfo;

/**
 * @author Guido Steinacker
 * @since 16.07.15
 */
public interface JobMonitor {
    /**
     * @deprecated use EventPublisher instead.
     */
    @Deprecated
    void update(JobInfo jobInfo);
}
