package de.otto.edison.dynamodb.configuration;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import de.otto.edison.annotations.Beta;
import de.otto.edison.dynamodb.jobs.DynamoJobMetaRepository;
import de.otto.edison.dynamodb.jobs.DynamoJobRepository;
import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.repository.JobRepository;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.jobs.configuration.JobsConfiguration")
@Beta
public class DynamoJobConfiguration {

  private static final Logger LOG = getLogger(DynamoJobConfiguration.class);

  @Bean
  public JobRepository jobRepository(final AmazonDynamoDB dynamoDB,
                                     final @Value("${edison.jobs.collection.jobinfo:jobinfo}") String collectionName) {
    LOG.info("===============================");
    LOG.info("Using DynamoJobRepository with {} MongoDatabase impl.", dynamoDB.getClass().getSimpleName());
    LOG.info("===============================");
    return new DynamoJobRepository(dynamoDB, collectionName);
  }

  @Bean
  public JobMetaRepository jobMetaRepository(final AmazonDynamoDB dynamoDB,
                                             final @Value("${edison.jobs.collection.jobmeta:jobmeta}")
                                               String collectionName) {
    LOG.info("===============================");
    LOG.info("Using DynamoJobMetaRepository with {} MongoDatabase impl.", dynamoDB.getClass().getSimpleName());
    LOG.info("===============================");
    return new DynamoJobMetaRepository(dynamoDB, collectionName);
  }
}
