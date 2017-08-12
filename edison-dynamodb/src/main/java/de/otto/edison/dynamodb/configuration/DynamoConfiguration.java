package de.otto.edison.dynamodb.configuration;

import static com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import de.otto.edison.annotations.Beta;

@Configuration
@EnableConfigurationProperties(DynamoProperties.class)
@Beta
public class DynamoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "dynamoDB", value = AmazonDynamoDB.class)
  AmazonDynamoDB amazonDynamoDB(final DynamoProperties dynamoProperties) {
    return AmazonDynamoDBClientBuilder.standard()
      .withEndpointConfiguration(new EndpointConfiguration(dynamoProperties.getEndpoint(), Regions.EU_CENTRAL_1.getName()))
      .withCredentials(new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(dynamoProperties.getAccessKey(), dynamoProperties.getSecretKey())))
      .build();
  }
}
