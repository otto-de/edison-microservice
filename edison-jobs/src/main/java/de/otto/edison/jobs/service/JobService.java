package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.domain.RunningJob;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.status.domain.SystemInfo;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static de.otto.edison.jobs.domain.JobInfo.*;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.*;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.domain.Level.INFO;
import static de.otto.edison.jobs.domain.Level.WARNING;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static java.lang.System.currentTimeMillis;
import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;

@Service
public class JobService {

    private static final Logger LOG = LoggerFactory.getLogger(JobService.class);

    private ApplicationEventPublisher applicationEventPublisher;
    private JobRepository jobRepository;
    private JobMetaService jobMetaService;
    private ScheduledExecutorService executor;
    private List<JobRunnable> jobRunnables = emptyList();
    private UuidProvider uuidProvider;

    private SystemInfo systemInfo;

    private Clock clock = Clock.systemDefaultZone();

    public JobService() {
    }

    @Autowired
    JobService(final JobRepository jobRepository,
               final JobMetaService jobMetaService,
               @Autowired(required = false) final List<JobRunnable> jobRunnables,
               final ScheduledExecutorService executor,
               final ApplicationEventPublisher applicationEventPublisher,
               @Autowired(required = false) final Clock clock,
               final SystemInfo systemInfo,
               final UuidProvider uuidProvider) {
        this.jobRepository = jobRepository;
        this.jobMetaService = jobMetaService;
        this.jobRunnables = jobRunnables != null ? jobRunnables : this.jobRunnables;
        this.executor = executor;
        this.applicationEventPublisher = applicationEventPublisher;
        this.clock = clock != null ? clock : this.clock;
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
            jobMetaService.aquireRunLock(jobInfo.getJobId(), jobInfo.getJobType());
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
        stopJob(jobId, null);
    }

    public void killJobsDeadSince(final int seconds) {
        final OffsetDateTime timeToMarkJobAsStopped = now(clock).minusSeconds(seconds);
        LOG.debug("JobCleanup: Looking for jobs older than {}.", timeToMarkJobAsStopped);
        final List<JobInfo> deadJobs = jobRepository.findRunningWithoutUpdateSince(timeToMarkJobAsStopped);
        deadJobs.forEach(deadJob -> {
            try {
                killJob(deadJob.getJobId());
            } catch (final Exception e) {
                LOG.error("Exception when kill dead job", e);
            }
        });
        clearRunLocks();
    }

    /**
     * Checks all run locks and releases the lock, if the job is stopped.
     * <p>
     * TODO: This method should never do something, otherwise the is a bug in the lock handling.
     * TODO: Check Log files + Remove
     */
    private void clearRunLocks() {
        jobMetaService.runningJobs().forEach((RunningJob runningJob) -> {
            final Optional<JobInfo> jobInfoOptional = jobRepository.findOne(runningJob.jobId());
            if (jobInfoOptional.isPresent() && jobInfoOptional.get().isStopped()) {
                jobMetaService.releaseRunLock(runningJob.jobType());
                LOG.error("Clear Lock of Job {}. Job stopped already.", runningJob.jobType());
            } else if (!jobInfoOptional.isPresent()) {
                jobMetaService.releaseRunLock(runningJob.jobType());
                LOG.error("Clear Lock of Job {}. JobID does not exist", runningJob.jobType());
            }
        });
    }

    public void killJob(final String jobId) {
        stopJob(jobId, DEAD);
        jobRepository.appendMessage(
                jobId,
                jobMessage(WARNING, "Job didn't receive updates for a while, considering it dead", now(clock))
        );
    }

    private void stopJob(final String jobId,
                         final JobStatus status) {
        jobRepository.findOne(jobId).ifPresent((JobInfo jobInfo) -> {
            jobMetaService.releaseRunLock(jobInfo.getJobType());
            final OffsetDateTime now = now(clock);
            final Builder builder = jobInfo.copy()
                    .setStopped(now)
                    .setLastUpdated(now);
            if (status != null) {
                builder.setStatus(status);
            }
            jobRepository.createOrUpdate(builder.build());
        });
    }

    public void appendMessage(final String jobId,
                              final JobMessage jobMessage) {
        writeMessageAndStatus(jobId, jobMessage.getLevel(), jobMessage.getMessage(), jobMessage.getLevel() == Level.ERROR ? ERROR : null, jobMessage.getTimestamp());
    }

    public void keepAlive(final String jobId) {
        jobRepository.setLastUpdate(jobId, now(clock));
    }

    public void markSkipped(final String jobId) {
        writeMessageAndStatus(jobId, INFO, "Skipped job ..", SKIPPED);
    }

    public void markRestarted(final String jobId) {
        writeMessageAndStatus(jobId, WARNING, "Restarting job ..", OK);
    }

    private void writeMessageAndStatus(final String jobId, Level messageLevel, String message, final JobStatus jobStatus) {
        OffsetDateTime currentTimestamp = now(clock);
        writeMessageAndStatus(jobId, messageLevel, message, jobStatus, currentTimestamp);
    }

    private void writeMessageAndStatus(final String jobId, Level messageLevel, String message, @Nullable final JobStatus jobStatus, OffsetDateTime timestamp) {
        // TODO: Refactor JobRepository so only a single update is required
        if (jobStatus != null) {
            jobRepository.setJobStatus(jobId, jobStatus);
        }
        jobRepository.appendMessage(jobId, jobMessage(messageLevel, message, timestamp));
    }

    private JobInfo createJobInfo(final String jobType) {
        return newJobInfo(uuidProvider.getUuid(), jobType, clock,
                systemInfo.getHostname());
    }

    private JobRunnable findJobRunnable(final String jobType) {
        final Optional<JobRunnable> optionalRunnable = jobRunnables.stream().filter(r -> r.getJobDefinition().jobType().equalsIgnoreCase(jobType)).findFirst();
        return optionalRunnable.orElseThrow(() -> new IllegalArgumentException("No JobRunnable for " + jobType));
    }

    private String startAsync(final JobRunnable jobRunnable,
                              final String jobId) {
        executor.execute(newJobRunner(
                jobId,
                jobRunnable,
                applicationEventPublisher,
                executor
        ));
        return jobId;
    }

    private JobRunnable metered(final JobRunnable delegate) {
        return new JobRunnable() {

            @Override
            public JobDefinition getJobDefinition() {
                return delegate.getJobDefinition();
            }

            @Override
            public boolean execute(JobEventPublisher jobEventPublisher) {
                long ts = currentTimeMillis();
                boolean executed = delegate.execute(jobEventPublisher);
                Metrics.gauge(gaugeName(), (currentTimeMillis() - ts) / 1000L);
                return executed;
            }

            private String gaugeName() {
                return "gauge.jobs.runtime." + delegate.getJobDefinition().jobType().toLowerCase();
            }
        };
    }

    public void handleTooBigJobLogs() {
        jobMetaService.runningJobs().forEach((RunningJob runningJob) -> {
            LOG.debug("Keeping job messages small for job id {}", runningJob.jobId());
            jobRepository.keepJobMessagesWithinMaximumSize(runningJob.jobId());
        });
    }
}
