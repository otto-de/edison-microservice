package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;

import static de.otto.edison.jobs.service.JobRunner.newJobRunner;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public class DefaultJobService implements JobService {

    @Autowired
    private JobFactory jobFactory;
    @Autowired
    private JobRepository repository;

    public DefaultJobService() {
    }

    DefaultJobService(final JobFactory jobFactory, final JobRepository jobRepository) {
        this.jobFactory = jobFactory;
        this.repository = jobRepository;
    }

    @Override
    public URI startAsyncJob(final JobRunnable jobRunnable) {
        final JobInfo job = jobFactory.createJobInfo(jobRunnable.getJobType());
        newJobRunner(job, repository).startAsync(jobRunnable);
        return job.getJobUri();
    }
}
