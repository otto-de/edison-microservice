package de.otto.edison.mongo.configuration;

import com.mongodb.client.MongoDatabase;
import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.mongo.jobs.MongoJobMetaRepository;
import de.otto.edison.mongo.jobs.MongoJobRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.jobs.configuration.JobsConfiguration")
public class MongoJobsConfiguration {

    private static final Logger LOG = getLogger(MongoJobsConfiguration.class);

    @Bean
    public JobRepository jobRepository(final MongoDatabase mongoDatabase, final @Value("${edison.jobs.collection.jobinfo:jobinfo}") String collectionName) {
        LOG.info("===============================");
        LOG.info("Using MongoJobRepository with %s MongoDatabase impl.", mongoDatabase.getClass().getSimpleName());
        LOG.info("===============================");
        return new MongoJobRepository(mongoDatabase, collectionName);
    }

    @Bean
    public JobMetaRepository jobMetaRepository(final MongoDatabase mongoDatabase, final @Value("${edison.jobs.collection.jobmeta:jobmeta}") String collectionName) {
        LOG.info("===============================");
        LOG.info("Using MongoJobMetaRepository with %s MongoDatabase impl.", mongoDatabase.getClass().getSimpleName());
        LOG.info("===============================");
        return new MongoJobMetaRepository(mongoDatabase, collectionName);
    }

}
