package de.otto.edison.jobs.configuration;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.jobs.monitor.JobMonitor;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.cleanup.KeepLastJobs;
import de.otto.edison.jobs.repository.cleanup.StopDeadJobs;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import de.otto.edison.jobs.repository.mongo.MongoJobRepository;
import de.otto.edison.jobs.service.DefaultJobService;
import de.otto.edison.jobs.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import static java.time.Clock.systemDefaultZone;
import static java.util.concurrent.Executors.newScheduledThreadPool;

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

    @Bean
    @ConditionalOnMissingBean(ScheduledExecutorService.class)
    public ScheduledExecutorService scheduledExecutorService() {
        return newScheduledThreadPool(numberOfThreads);
    }

    @Bean
    @ConditionalOnClass(MongoClient.class)
    @ConditionalOnProperty("edison.mongo.db")
    public JobRepository mongoJobRepository() {
        LOG.info("Using MongoDb JobRepository");
        return new MongoJobRepository();
    }

    @Bean
    @ConditionalOnMissingBean(JobRepository.class)
    public JobRepository inMemJobRepository() {
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
}
