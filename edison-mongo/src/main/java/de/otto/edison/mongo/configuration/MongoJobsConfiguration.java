package de.otto.edison.mongo.configuration;

import com.mongodb.client.MongoDatabase;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.JobStateRepository;
import de.otto.edison.mongo.jobs.MongoJobRepository;
import de.otto.edison.mongo.jobs.MongoJobStateRepository;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.jobs.configuration.JobsConfiguration")
public class MongoJobsConfiguration {

    private static final Logger LOG = getLogger(MongoJobsConfiguration.class);

    @Bean
    public JobRepository jobRepository(final MongoDatabase mongoDatabase) {
        LOG.info("===============================");
        LOG.info("Using MongoJobRepository with %s MongoDatabase impl.", mongoDatabase.getClass().getSimpleName());
        LOG.info("===============================");
        return new MongoJobRepository(mongoDatabase);
    }

    @Bean
    public JobStateRepository jobStateRepository(final MongoDatabase mongoDatabase) {
        LOG.info("===============================");
        LOG.info("Using MongoJobLockRepository with %s MongoDatabase impl.", mongoDatabase.getClass().getSimpleName());
        LOG.info("===============================");
        return new MongoJobStateRepository(mongoDatabase);
    }

}
