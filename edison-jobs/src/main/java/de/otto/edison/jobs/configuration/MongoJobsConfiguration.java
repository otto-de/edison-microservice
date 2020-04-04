package de.otto.edison.jobs.configuration;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.mongo.MongoJobMetaRepository;
import de.otto.edison.jobs.repository.mongo.MongoJobRepository;
import de.otto.edison.mongo.configuration.MongoProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnProperty(prefix = "edison.jobs", name = "mongo.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(MongoClient.class)
public class MongoJobsConfiguration {

    private static final Logger LOG = getLogger(MongoJobsConfiguration.class);

    @Bean
    public JobRepository jobRepository(final MongoDatabase mongoDatabase,
                                       final @Value("${edison.jobs.collection.jobinfo:jobinfo}") String collectionName,
                                       final MongoProperties mongoProperties) {
        LOG.info("===============================");
        LOG.info("Using MongoJobRepository with {} MongoDatabase impl.", mongoDatabase.getClass().getSimpleName());
        LOG.info("===============================");
        return new MongoJobRepository(mongoDatabase, collectionName, mongoProperties);
    }

    @Bean
    public JobMetaRepository jobMetaRepository(final MongoDatabase mongoDatabase,
                                               final @Value("${edison.jobs.collection.jobmeta:jobmeta}") String collectionName,
                                               final MongoProperties mongoProperties) {
        LOG.info("===============================");
        LOG.info("Using MongoJobMetaRepository with {} MongoDatabase impl.", mongoDatabase.getClass().getSimpleName());
        LOG.info("===============================");
        return new MongoJobMetaRepository(mongoDatabase, collectionName, mongoProperties);
    }

}
