package de.otto.edison.jobs.configuration;

import de.otto.edison.jobs.domain.JobType;
import de.otto.edison.jobs.repository.*;
import de.otto.edison.jobs.service.DefaultJobService;
import de.otto.edison.jobs.service.JobService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newScheduledThreadPool;

@Configuration
@EnableAsync
@EnableScheduling
public class JobConfiguration {

    public static final int N_THREADS = 10;
    public static final int NUMBER_OF_JOBS_TO_KEEP = 100;

    @Bean
    @ConditionalOnMissingBean(ScheduledExecutorService.class)
    public ScheduledExecutorService scheduledExecutorService() {
        return newScheduledThreadPool(N_THREADS);
    }

    @Bean
    @ConditionalOnMissingBean(JobRepository.class)
    public JobRepository jobRepository() {
        return new InMemJobRepository();
    }

    @Bean
    @ConditionalOnMissingBean(JobService.class)
    public JobService jobService() { return new DefaultJobService(); }

    @Bean
    @ConditionalOnMissingBean(JobRepositoryCleanup.class)
    public JobRepositoryCleanup jobRepositoryCleanup() {
        return new JobRepositoryCleanup();
    }

    @Bean
    @ConditionalOnMissingBean(JobCleanupStrategy.class)
    public JobCleanupStrategy jobCleanupStrategy() {
        return new KeepLastJobs(NUMBER_OF_JOBS_TO_KEEP, Optional.<JobType>empty());
    }

}
