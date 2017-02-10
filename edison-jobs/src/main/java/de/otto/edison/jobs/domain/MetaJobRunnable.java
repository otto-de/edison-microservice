package de.otto.edison.jobs.domain;

import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.service.JobMetaService;
import de.otto.edison.jobs.service.JobRunnable;

/**
 * Abstract class used to implement stateful jobs.
 * <p>
 *     Provides access to meta information that is persisted between job executions.
 *     Meta data is stored in a {@link JobMetaRepository}.
 * </p>
 */
public abstract class MetaJobRunnable implements JobRunnable {

    private final JobMeta jobMeta;

    protected MetaJobRunnable(final String jobType,
                              final JobMetaService jobMetaService) {
        this.jobMeta = jobMetaService.getJobMeta(jobType);
    }

    /**
     * Provides access to the meta data of the job type.
     *
     * @return Meta
     */
    protected JobMeta jobMeta() {
        return jobMeta;
    }

}
