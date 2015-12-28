package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static de.otto.edison.jobs.eventbus.JobEventPublisher.newJobEventPublisher;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static java.lang.System.currentTimeMillis;
import static java.net.URI.create;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public class DefaultJobService implements JobService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultJobService.class);

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private JobRepository repository;
    @Autowired
    private ScheduledExecutorService executor;
    @Autowired
    private GaugeService gaugeService;
    @Autowired(required = false)
    private List<JobRunnable> jobRunnables = emptyList();


    public DefaultJobService() {
    }

    DefaultJobService(final JobRepository repository,
                      final List<JobRunnable> jobRunnables,
                      final GaugeService gaugeService,
                      final ScheduledExecutorService executor,
                      final ApplicationEventPublisher applicationEventPublisher) {
        this.repository = repository;
        this.repository = repository;
        this.jobRunnables = jobRunnables;
        this.gaugeService = gaugeService;
        this.executor = executor;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostConstruct
    public void postConstruct() {
        LOG.info("Found {} JobRunnables: {}", +jobRunnables.size(), jobRunnables.stream().map(j -> j.getJobDefinition().jobType()).collect(Collectors.toList()));
    }

    @Override
    public Optional<URI> startAsyncJob(String jobType) {
        final JobRunnable jobRunnable = findJobRunnable(jobType);
        // TODO: use some kind of database lock so we can prevent race conditions
        final Optional<JobInfo> alreadyRunning = repository.findRunningJobByType(jobRunnable.getJobDefinition().jobType());
        if (alreadyRunning == null || !alreadyRunning.isPresent()) {
            return Optional.of(startAsync(metered(jobRunnable)));
        } else {
            final URI jobUri = alreadyRunning.get().getJobUri();
            LOG.info("Job {} triggered but not started - still running.", jobUri);
            return Optional.empty();
        }
    }

    @Override
    public Optional<JobInfo> findJob(final URI uri) {
        return repository.findOne(uri);
    }

    @Override
    public List<JobInfo> findJobs(final Optional<String> type, final int count) {
        if (type.isPresent()) {
            return repository.findLatestBy(type.get(), count);
        } else {
            return repository.findLatest(count);
        }
    }

    @Override
    public void deleteJobs(final Optional<String> type) {
        if (type.isPresent()) {
            repository.findByType(type.get()).forEach((j) -> repository.removeIfStopped(j.getJobUri()));
        } else {
            repository.findAll().forEach((j) -> repository.removeIfStopped(j.getJobUri()));
        }
    }

    private JobRunnable findJobRunnable(String jobType) {
        final Optional<JobRunnable> optionalRunnable = jobRunnables.stream().filter((r) -> r.getJobDefinition().jobType().equalsIgnoreCase(jobType)).findFirst();
        return optionalRunnable.orElseThrow(() -> new IllegalArgumentException("No JobRunnable for " + jobType));
    }

    private URI startAsync(final JobRunnable jobRunnable) {
        final URI jobUri = newJobUri();
        final String jobType = jobRunnable.getJobDefinition().jobType();
        final JobRunner jobRunner = newJobRunner(
                jobUri,
                jobType,
                executor,
                newJobEventPublisher(applicationEventPublisher, jobRunnable, jobUri)
        );
        executor.execute(() -> jobRunner.start(jobRunnable));
        return jobUri;
    }

    private JobRunnable metered(final JobRunnable delegate) {
        return new JobRunnable() {

            @Override
            public JobDefinition getJobDefinition() {
                return delegate.getJobDefinition();
            }

            @Override
            public void execute(final JobEventPublisher jobEventPublisher) {
                long ts = currentTimeMillis();
                delegate.execute(jobEventPublisher);
                gaugeService.submit(gaugeName(), (currentTimeMillis() - ts) / 1000L);
            }

            private String gaugeName() {
                return "gauge.jobs.runtime." + delegate.getJobDefinition().jobType().toLowerCase();
            }
        };
    }

    private URI newJobUri() {
        return create("/internal/jobs/" + randomUUID());
    }
}
