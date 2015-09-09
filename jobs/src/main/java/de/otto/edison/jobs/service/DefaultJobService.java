package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.monitor.JobMonitor;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static java.lang.System.currentTimeMillis;
import static java.net.URI.create;
import static java.time.Clock.systemDefaultZone;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;

/**
 * @author Guido Steinacker
 * @since 15.02.15
 */
public class DefaultJobService implements JobService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultJobService.class);

    @Autowired
    private JobMonitor monitor;
    @Autowired
    private JobRepository repository;
    @Autowired
    private ScheduledExecutorService executor;
    @Autowired
    private GaugeService gaugeService;
    @Autowired(required = false)
    private List<JobRunnable> jobRunnables = emptyList();


    private final Clock clock;

    public DefaultJobService() {
        this.clock = systemDefaultZone();
    }

    DefaultJobService(final JobRepository repository,
                      final JobMonitor monitor,
                      final List<JobRunnable> jobRunnables,
                      final GaugeService gaugeService,
                      final Clock clock,
                      final ScheduledExecutorService executor) {
        this.repository = repository;
        this.monitor = monitor;
        this.repository = repository;
        this.jobRunnables = jobRunnables;
        this.gaugeService = gaugeService;
        this.clock = clock;
        this.executor = executor;
    }

    @PostConstruct
    public void postConstruct() {
        LOG.info("Found {} JobRunnables: {}", +jobRunnables.size(), jobRunnables.stream().map(JobRunnable::getJobType).collect(Collectors.toList()));
    }

    @Override
    public Optional<URI> startAsyncJob(String jobType) {
        final Optional<JobRunnable> jobRunnable = jobRunnables.stream().filter((r) -> r.getJobType().equalsIgnoreCase(jobType)).findFirst();
        return startAsyncJob(jobRunnable.orElseThrow(() -> new IllegalArgumentException("No JobRunnable for " + jobType)));
    }

    @Override
    public Optional<URI> startAsyncJob(final JobRunnable jobRunnable) {
        // TODO: use some kind of database lock so we can prevent race conditions
        final Optional<JobInfo> alreadyRunning = repository.findRunningJobByType(jobRunnable.getJobType());
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
            repository.findByType(type.get()).forEach((j)-> repository.removeIfStopped(j.getJobUri()));
        } else {
            repository.findAll().forEach((j)-> repository.removeIfStopped(j.getJobUri()));
        }
    }

    private URI startAsync(final JobRunnable jobRunnable) {
        final JobInfo jobInfo = newJobInfo(newJobUri(), jobRunnable.getJobType(), monitor, clock);
        final JobRunner jobRunner = newJobRunner(jobInfo, repository, executor);
        executor.execute(() -> jobRunner.start(jobRunnable));
        return jobInfo.getJobUri();
    }

    private JobRunnable metered(final JobRunnable delegate) {
        return new JobRunnable() {
            @Override
            public String getJobType() {
                return delegate.getJobType();
            }

            @Override
            public void execute(final JobInfo jobInfo) {
                long ts = currentTimeMillis();
                delegate.execute(jobInfo);
                gaugeService.submit(gaugeName(), (currentTimeMillis()-ts)/1000L);
            }

            private String gaugeName() {
                return "gauge.jobs.runtime." + delegate.getJobType().toLowerCase();
            }
        };
    }

    private URI newJobUri() {
        return create("/internal/jobs/" + randomUUID());
    }
}
