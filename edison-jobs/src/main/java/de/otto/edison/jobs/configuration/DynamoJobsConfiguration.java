package de.otto.edison.jobs.configuration;

import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.dynamo.DynamoJobMetaRepository;
import de.otto.edison.jobs.repository.dynamo.DynamoJobRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnProperty(prefix = "edison.jobs", name = "dynamo.enabled", havingValue = "true")
@ConditionalOnBean(type = "software.amazon.awssdk.services.dynamodb.DynamoDbClient")
public class DynamoJobsConfiguration {

    private static final Logger LOG = getLogger(DynamoJobsConfiguration.class);

    @Bean
    public JobRepository jobRepository(final DynamoDbClient dynamoDbClient,
                                       final @Value("${edison.jobs.dynamo.jobinfo.tableName}") String tableName,
                                       final @Value("${edison.jobs.dynamo.jobinfo.pageSize}") int pageSize) {
        LOG.info("===============================");
        LOG.info("Using DynamoJobRepository with tableName {} and pageSize {}.",tableName, pageSize);
        LOG.info("===============================");
        return new DynamoJobRepository(dynamoDbClient, tableName, pageSize);
    }

    @Bean
    public JobMetaRepository jobMetaRepository(final DynamoDbClient dynamoDbClient,
                                               final @Value("${edison.jobs.dynamo.jobmeta.tableName}") String tableName) {
        LOG.info("===============================");
        LOG.info("Using DynamoJobMetaRepository with tableName {}.", tableName);
        LOG.info("===============================");
        return new DynamoJobMetaRepository(dynamoDbClient, tableName);
    }

}
