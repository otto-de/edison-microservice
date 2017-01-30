package de.otto.edison.mongo.configuration;

import com.mongodb.client.MongoDatabase;
import de.otto.edison.jobs.repository.JobLockRepository;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.service.JobMutexGroup;
import de.otto.edison.jobs.service.JobMutexGroups;
import de.otto.edison.mongo.jobs.MongoJobLockRepository;
import de.otto.edison.mongo.jobs.MongoJobRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

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
    public JobLockRepository jobLockRepository(final MongoDatabase mongoDatabase, final JobMutexGroups jobMutexGroups) {
        LOG.info("===============================");
        LOG.info("Using MongoJobLockRepository with %s MongoDatabase impl.", mongoDatabase.getClass().getSimpleName());
        LOG.info("===============================");
        return new MongoJobLockRepository(mongoDatabase, jobMutexGroups);
    }

}
