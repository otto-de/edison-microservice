package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;
import de.otto.edison.jobs.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.GaugeService;

import java.net.URI;
import java.util.concurrent.Executor;

import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static java.lang.System.currentTimeMillis;
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
    @Autowired
    private GaugeService gaugeService;
    @Autowired
    private Clock clock;

    @Value("${server.contextPath}")
    private String serverContextPath;

    public DefaultJobService() {
    }

    DefaultJobService(final String serverContextPath, final JobRepository jobRepository, final GaugeService gaugeService, final Clock clock, final Executor executor) {
        this.serverContextPath = serverContextPath;
        this.repository = jobRepository;
        this.executor = executor;
        this.gaugeService = gaugeService;
        this.clock = clock;
    }

    @Override
    public URI startAsyncJob(final JobRunnable jobRunnable) {
        return startAsync(metered(jobRunnable));
    }

    private URI startAsync(final JobRunnable jobRunnable) {
        final JobInfo jobInfo = jobInfoBuilder(jobRunnable.getJobType(), newJobUri()).build();
        executor.execute(() -> newJobRunner(jobInfo, repository, clock).start(jobRunnable));
        return jobInfo.getJobUri();
    }



    private JobRunnable metered(final JobRunnable delegate) {
        return new JobRunnable() {
            @Override
            public JobType getJobType() {
                return delegate.getJobType();
            }

            @Override
            public void execute(final JobLogger logger) {
                long ts = currentTimeMillis();
                delegate.execute(logger);
                gaugeService.submit(gaugeName(), (currentTimeMillis()-ts)/1000L);
            }

            private String gaugeName() {
                return "gauge.jobs.runtime." + delegate.getJobType().name().toLowerCase();
            }
        };
    }

    private URI newJobUri() {
        return create(serverContextPath + "/jobs/" + randomUUID());
    }
}
