package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobLockRepository;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.status.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.ERROR;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.eventbus.JobEventPublisher.newJobEventPublisher;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;

@Service
public class JobService {

    private static final Logger LOG = LoggerFactory.getLogger(JobService.class);

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobLockRepository lockRepository;
    @Autowired
    private ScheduledExecutorService executor;
    @Autowired
    private GaugeService gaugeService;
    @Autowired(required = false)
    private List<JobRunnable> jobRunnables = emptyList();
    @Autowired
    private UuidProvider uuidProvider;


    @Autowired
    private SystemInfo systemInfo;

    private Clock clock = Clock.systemDefaultZone();

    public JobService() {
    }

    JobService(final JobRepository jobRepository,
               final JobLockRepository lockRepository,
               final List<JobRunnable> jobRunnables,
               final GaugeService gaugeService,
               final ScheduledExecutorService executor,
               final ApplicationEventPublisher applicationEventPublisher,
               final Clock clock,
               final SystemInfo systemInfo,
               final UuidProvider uuidProvider) {
        this.jobRepository = jobRepository;
        this.lockRepository = lockRepository;
        this.jobRunnables = jobRunnables;
        this.gaugeService = gaugeService;
        this.executor = executor;
        this.applicationEventPublisher = applicationEventPublisher;
        this.clock = clock;
        this.systemInfo = systemInfo;
        this.uuidProvider = uuidProvider;
    }

    @PostConstruct
    public void postConstruct() {
        LOG.info("Found {} JobRunnables: {}", +jobRunnables.size(), jobRunnables.stream().map(j -> j.getJobDefinition().jobType()).collect(Collectors.toList()));
    }

    /**
     * Starts a job asynchronously in the background.
     *
     * @param jobType the type of the job
     * @return the URI under which you can retrieve the status about the triggered job instance
     */
    public Optional<String> startAsyncJob(String jobType) {
        try {
            final JobRunnable jobRunnable = findJobRunnable(jobType);
            final JobInfo jobInfo = createJobInfo(jobType);
            lockRepository.aquireRunLock(jobInfo.getJobId(), jobInfo.getJobType());
            jobRepository.createOrUpdate(jobInfo);
            return Optional.of(startAsync(metered(jobRunnable), jobInfo.getJobId()));
        } catch (JobBlockedException e) {
            LOG.info(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<JobInfo> findJob(final String id) {
        return jobRepository.findOne(id);
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
            return jobRepository.findLatestBy(type.get(), count);
        } else {
            return jobRepository.findLatest(count);
        }
    }

    public List<JobInfo> findJobsDistinct() {
        return jobRepository.findLatestJobsDistinct();
    }

    public void deleteJobs(final Optional<String> type) {
        if (type.isPresent()) {
            jobRepository.findByType(type.get()).forEach((j) -> jobRepository.removeIfStopped(j.getJobId()));
        } else {
            jobRepository.findAll().forEach((j) -> jobRepository.removeIfStopped(j.getJobId()));
        }
    }

    public void stopJob(final String jobId) {
        this.stopJob(jobId, Optional.empty());
    }

    public void killJobsDeadSince(final int seconds) {
        final OffsetDateTime timeToMarkJobAsStopped = now(clock).minusSeconds(seconds);
        LOG.info(format("JobCleanup: Looking for jobs older than %s ", timeToMarkJobAsStopped));
        final List<JobInfo> deadJobs = jobRepository.findRunningWithoutUpdateSince(timeToMarkJobAsStopped);
        deadJobs.forEach(deadJob -> killJob(deadJob.getJobId()));
    }

    public void killJob(String jobId) {
        this.stopJob(jobId, Optional.of(JobInfo.JobStatus.DEAD));
        jobRepository.appendMessage(jobId, jobMessage(Level.WARNING, "Job didn't receive updates for a while, considering it dead", OffsetDateTime.now(clock)));
    }

    private void stopJob(final String jobId, Optional<JobInfo.JobStatus> status) {
        Optional<JobInfo> optionalJobInfo = jobRepository.findOne(jobId);
        if (!optionalJobInfo.isPresent()) {
            return;
        }
        JobInfo jobInfo = optionalJobInfo.get();
        lockRepository.releaseRunLock(jobInfo.getJobType());
        OffsetDateTime now = OffsetDateTime.now(clock);
        JobInfo.Builder builder = jobInfo.copy()
                .setStopped(now)
                .setLastUpdated(now);
        status.ifPresent(builder::setStatus);
        jobRepository.createOrUpdate(builder.build());
    }


    public void appendMessage(String jobId, JobMessage jobMessage) {
        jobRepository.appendMessage(jobId, jobMessage);
        if (jobMessage.getLevel() == Level.ERROR) {
            jobRepository.findOne(jobId).ifPresent(jobInfo -> {
                jobRepository.createOrUpdate(
                        jobInfo.copy()
                                .setStatus(ERROR)
                                .setLastUpdated(now(clock))
                                .build());
            });
        }
    }

    public void keepAlive(String jobId) {
        jobRepository.setLastUpdate(jobId, now(clock));
    }

    public void markSkipped(String jobId) {
        OffsetDateTime currentTimestamp = now(clock);
        jobRepository.appendMessage(jobId, jobMessage(Level.INFO, "Skipped job ..", currentTimestamp));
        jobRepository.setLastUpdate(jobId, currentTimestamp);
        jobRepository.setJobStatus(jobId, JobInfo.JobStatus.SKIPPED);
    }

    public void markRestarted(String jobId) {
        OffsetDateTime currentTimestamp = now(clock);
        jobRepository.appendMessage(jobId, jobMessage(Level.WARNING, "Restarting job ..", currentTimestamp));
        jobRepository.setLastUpdate(jobId, currentTimestamp);
        jobRepository.setJobStatus(jobId, JobInfo.JobStatus.OK);
    }

    private JobInfo createJobInfo(String jobType) {
        return newJobInfo(uuidProvider.getUuid(), jobType, clock,
                systemInfo.getHostname());
    }

    private JobRunnable findJobRunnable(String jobType) {
        final Optional<JobRunnable> optionalRunnable = jobRunnables.stream().filter(r -> r.getJobDefinition().jobType().equalsIgnoreCase(jobType)).findFirst();
        return optionalRunnable.orElseThrow(() -> new IllegalArgumentException("No JobRunnable for " + jobType));
    }

    private String startAsync(final JobRunnable jobRunnable, final String jobId) {
        final JobRunner jobRunner = createJobRunner(jobRunnable, jobId);
        executor.execute(() -> jobRunner.start(jobRunnable));
        return jobId;
    }

    private JobRunner createJobRunner(JobRunnable jobRunnable, String jobId) {
        final String jobType = jobRunnable.getJobDefinition().jobType();
        return newJobRunner(
                jobId,
                jobType,
                executor,
                newJobEventPublisher(applicationEventPublisher, jobRunnable, jobId)
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

    public void disableJobType(String jobType) {
        lockRepository.disableJobType(jobType);
    }

    public void enableJobType(String jobType) {
        lockRepository.enableJobType(jobType);
    }
}
