package de.otto.edison.jobs.configuration;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.monitor.JobMonitor;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.cleanup.KeepLastJobs;
import de.otto.edison.jobs.repository.cleanup.StopDeadJobs;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import de.otto.edison.jobs.service.DefaultJobService;
import de.otto.edison.jobs.service.JobService;
import de.otto.edison.jobs.status.JobStatusDetailIndicator;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.CompositeStatusDetailIndicator;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import static java.time.Clock.systemDefaultZone;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.stream.Collectors.toList;

@Configuration
@EnableAsync
@EnableScheduling
public class JobConfiguration {

    public static final Logger LOG = LoggerFactory.getLogger(JobConfiguration.class);

    @Value("${edison.jobs.scheduler.thread-count:10}")
    int numberOfThreads;

    @Value("${edison.jobs.cleanup.number-to-keep:100}")
    int numberOfJobsToKeep;

    @Value("${edison.jobs.cleanup.mark-dead-after:20}")
    int secondsToMarkJobsAsDead;

    @Autowired(required = false)
    List<JobDefinition> jobDefinitions = new ArrayList<>();

    @Bean
    @ConditionalOnMissingBean(ScheduledExecutorService.class)
    public ScheduledExecutorService scheduledExecutorService() {
        return newScheduledThreadPool(numberOfThreads);
    }

    @Bean
    @ConditionalOnMissingBean(JobRepository.class)
    public JobRepository jobRepository() {
        LOG.warn("Using in-memory JobRepository");
        return new InMemJobRepository();
    }

    @Bean
    @ConditionalOnMissingBean(JobService.class)
    public JobService jobService() {
        return new DefaultJobService();
    }

    @Bean
    @ConditionalOnMissingBean(KeepLastJobs.class)
    public KeepLastJobs keepLastJobsStrategy() {
        return new KeepLastJobs(numberOfJobsToKeep, Optional.empty());
    }

    @Bean
    @ConditionalOnMissingBean(StopDeadJobs.class)
    public StopDeadJobs deadJobStrategy() {
        return new StopDeadJobs(secondsToMarkJobsAsDead, systemDefaultZone());
    }

    @Bean
    @ConditionalOnMissingBean(JobMonitor.class)
    public JobMonitor jobMonitor() {
        return jobInfo -> { /* no-op */ };
    }

    @Bean
    @ConditionalOnProperty(name = "edison.jobs.status.enabled", havingValue = "true", matchIfMissing = true)
    public StatusDetailIndicator jobStatusDetailIndicator() {
        if (jobDefinitions == null || jobDefinitions.isEmpty()) {
            return () -> StatusDetail.statusDetail("jobs", Status.OK, "No job definitions configured in application.");
        } else {
            return new CompositeStatusDetailIndicator("jobs",
                    jobDefinitions
                            .stream()
                            .map(d -> new JobStatusDetailIndicator(jobRepository(), d.jobName(), d.jobType(), d.maxAge()))
                            .collect(toList())
            );
        }
    }
}
