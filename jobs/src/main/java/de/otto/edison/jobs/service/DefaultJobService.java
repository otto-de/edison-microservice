package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.util.concurrent.Executor;

import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static java.net.URI.create;
import static java.util.UUID.randomUUID;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public class DefaultJobService implements JobService {

    @Autowired
    private JobRepository repository;
    @Autowired
    private Executor executor;

    @Value("${server.contextPath}")
    private String serverContextPath;

    public DefaultJobService() {
    }

    DefaultJobService(final String serverContextPath, final JobRepository jobRepository, final Executor executor) {
        this.serverContextPath = serverContextPath;
        this.repository = jobRepository;
        this.executor = executor;
    }

    @Override
    public URI startAsyncJob(final JobRunnable jobRunnable) {
        final JobInfo jobInfo = jobInfoBuilder(jobRunnable.getJobType(), newJobUri()).build();
        executor.execute(() -> newJobRunner(jobInfo, repository).start(jobRunnable));
        return jobInfo.getJobUri();
    }

    private URI newJobUri() {
        return create(serverContextPath + "/jobs/" + randomUUID());
    }
}
