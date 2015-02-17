package de.otto.µservice.jobs.configuration;

import de.otto.µservice.jobs.repository.InMemJobRepository;
import de.otto.µservice.jobs.repository.JobRepository;
import de.otto.µservice.jobs.service.DefaultJobService;
import de.otto.µservice.jobs.service.JobFactory;
import de.otto.µservice.jobs.service.JobService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class JobConfiguration {

    @Bean
    @ConditionalOnMissingBean(JobRepository.class)
    public JobRepository jobRepository() {
        return new InMemJobRepository();
    }

    @Bean
    @ConditionalOnMissingBean(JobFactory.class)
    public JobFactory jobFactory() {
        return new JobFactory();
    }

    @Bean
    @ConditionalOnMissingBean(JobService.class)
    public JobService jobService() { return new DefaultJobService(); }
}
