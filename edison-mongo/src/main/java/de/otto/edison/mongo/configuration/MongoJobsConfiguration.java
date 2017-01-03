package de.otto.edison.mongo.configuration;

import com.mongodb.client.MongoDatabase;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.mongo.jobs.MongoJobRepository;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.mongo.jobs.configuration.JobConfiguration")
public class MongoJobsConfiguration {

    private static final Logger LOG = getLogger(MongoJobsConfiguration.class);

    @Bean
    public JobRepository jobRepository(final MongoDatabase mongoDatabase) {
        return new MongoJobRepository(mongoDatabase);
    }

}
