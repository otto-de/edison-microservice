package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static de.otto.edison.jobs.eventbus.JobEventPublisher.newJobEventPublisher;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;

/**
 * A service used to manage jobs in Edison microservices.
 *
 * @author Guido Steinacker
 * @since 15.02.15
 */
@Service
public class JobService {

    private static final Logger LOG = LoggerFactory.getLogger(JobService.class);

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
    @Autowired
    private JobMutexHandler jobMutexHandler;


    public JobService() {
    }

    JobService(final JobRepository repository,
               final List<JobRunnable> jobRunnables,
               final GaugeService gaugeService,
               final ScheduledExecutorService executor,
               final ApplicationEventPublisher applicationEventPublisher,
               final JobMutexHandler jobMutexHandler) {
        this.repository = repository;
        this.jobRunnables = jobRunnables;
        this.gaugeService = gaugeService;
        this.executor = executor;
        this.applicationEventPublisher = applicationEventPublisher;
        this.jobMutexHandler = jobMutexHandler;
    }

    @PostConstruct
    public void postConstruct() {
        LOG.info("Found {} JobRunnables: {}", +jobRunnables.size(), jobRunnables.stream().map(j -> j.getJobDefinition().jobType()).collect(Collectors.toList()));
    }

    /**
     * Starts a job synchronously, and blocks until the job has finished execution.
     *
     * @param jobType the type of the job
     * @return the URI to retrieve detail information about the executed job instance
     */
    public Optional<String> startJob(String jobType) {
        return startJob(jobType, false);
    }

    /**
     * Starts a job asynchronously in the background.
     *
     * @param jobType the type of the job
     * @return the URI under which you can retrieve the status about the triggered job instance
     */
    public Optional<String> startAsyncJob(String jobType) {
        return startJob(jobType, true);
    }

    private Optional<String> startJob(String jobType, boolean async) {
        final JobRunnable jobRunnable = findJobRunnable(jobType);
        if (jobMutexHandler.isJobStartable(jobRunnable.getJobDefinition().jobType())) {
            if (async) {
                return Optional.of(startAsync(metered(jobRunnable)));
            } else {
                return Optional.of(start(metered(jobRunnable)));
            }
        } else {
            LOG.info("Job {} triggered but blocked by other job.", jobType);
            return Optional.empty();
        }
    }

    public Optional<JobInfo> findJob(final String id) {
        return repository.findOne(id);
    }

    /**
     * Find the latest jobs, optionally restricted to jobs of a specified type.
     *
     * @param type  if provided, the last N jobs of the type are returned, otherwise the last jobs of any type.
     * @param count the number of jobs to return.
     * @return a list of JobInfos
     */
    public List<JobInfo> findJobs(final Optional<String> type, final int count) {
        if (type.isPresent()) {
            return repository.findLatestBy(type.get(), count);
        } else {
            return repository.findLatest(count);
        }
    }

    public List<JobInfo> findFinishedJobs(final String type, JobStatus status, final int count) {
        return repository.findLatestFinishedBy(type, status, count);
    }

    public void deleteJobs(final Optional<String> type) {
        if (type.isPresent()) {
            repository.findByType(type.get()).forEach((j) -> repository.removeIfStopped(j.getJobId()));
        } else {
            repository.findAll().forEach((j) -> repository.removeIfStopped(j.getJobId()));
        }
    }


    private JobRunnable findJobRunnable(String jobType) {
        final Optional<JobRunnable> optionalRunnable = jobRunnables.stream().filter((r) -> r.getJobDefinition().jobType().equalsIgnoreCase(jobType)).findFirst();
        return optionalRunnable.orElseThrow(() -> new IllegalArgumentException("No JobRunnable for " + jobType));
    }

    private String startAsync(final JobRunnable jobRunnable) {
        final String jobId = newJobId();
        final JobRunner jobRunner = createJobRunner(jobRunnable, jobId);
        executor.execute(() -> jobRunner.start(jobRunnable));
        return jobId;
    }

    private String start(final JobRunnable jobRunnable) {
        final String jobUri = newJobId();
        final JobRunner jobRunner = createJobRunner(jobRunnable, jobUri);
        jobRunner.start(jobRunnable);
        return jobUri;
    }

    private JobRunner createJobRunner(JobRunnable jobRunnable, String jobId) {
        final String jobType = jobRunnable.getJobDefinition().jobType();
        return newJobRunner(
                jobId,
                jobType,
                executor,
                newJobEventPublisher(applicationEventPublisher, jobRunnable, jobId),
                jobMutexHandler
        );
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

    private String newJobId() {
        return randomUUID().toString();
    }

}
