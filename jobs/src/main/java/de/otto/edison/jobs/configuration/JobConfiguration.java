package de.otto.edison.jobs.configuration;

import de.otto.edison.jobs.repository.InMemJobRepository;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.service.DefaultJobService;
import de.otto.edison.jobs.service.JobService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class JobConfiguration {

    @Bean
    @ConditionalOnMissingBean(JobRepository.class)
    public JobRepository jobRepository() {
        return new InMemJobRepository();
    }

    @Bean
    @ConditionalOnMissingBean(JobService.class)
    public JobService jobService() { return new DefaultJobService(); }

    @Bean
    @ConditionalOnMissingBean(ExecutorService.class)
    public ExecutorService executorService() {
        return Executors.newSingleThreadExecutor();
    }
}
