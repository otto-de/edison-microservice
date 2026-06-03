package de.otto.edison.jobs.configuration;

import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobMetaRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = {DynamoJobsConfiguration.class, MongoJobsConfiguration.class})
public class InMemoryJobsConfiguration {

    public static final Logger LOG = LoggerFactory.getLogger(InMemoryJobsConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(JobMetaRepository.class)
    public JobMetaRepository jobMetaRepository() {
        return new InMemJobMetaRepository();
    }

    @Bean
    @ConditionalOnMissingBean(JobRepository.class)
    public JobRepository jobRepository() {
        LOG.warn("===============================");
        LOG.warn("Using in-memory JobRepository");
        LOG.warn("===============================");
        return new InMemJobRepository();
    }

}
