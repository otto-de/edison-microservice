package de.otto.edison.mongo.configuration;

import com.mongodb.client.MongoDatabase;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.mongo.jobs.MongoJobRepository;
import de.otto.edison.mongo.jobs.MongoJobMetaRepository;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.mongo.jobs.MongoJobMetaRepository.JOBMETA_COLLECTION_NAME;
import static de.otto.edison.mongo.jobs.MongoJobRepository.JOB_INFO_COLLECTION_NAME;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.jobs.configuration.JobsConfiguration")
public class MongoJobsConfiguration {

    private static final Logger LOG = getLogger(MongoJobsConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(name = "jobRepository")
    public JobRepository jobRepository(final MongoDatabase mongoDatabase) {
        LOG.info("===============================");
        LOG.info("Using MongoJobRepository with %s MongoDatabase impl.", mongoDatabase.getClass().getSimpleName());
        LOG.info("===============================");
        return new MongoJobRepository(mongoDatabase, JOB_INFO_COLLECTION_NAME);
    }

    @Bean
    @ConditionalOnMissingBean(name = "jobMetaRepository")
    public JobMetaRepository jobMetaRepository(final MongoDatabase mongoDatabase) {
        LOG.info("===============================");
        LOG.info("Using MongoJobMetaRepository with %s MongoDatabase impl.", mongoDatabase.getClass().getSimpleName());
        LOG.info("===============================");
        return new MongoJobMetaRepository(mongoDatabase, JOBMETA_COLLECTION_NAME);
    }

}
