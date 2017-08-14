package de.otto.edison.dynamodb.configuration;


import de.otto.edison.annotations.Beta;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.AwsCredentials;
import software.amazon.awssdk.auth.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;

import java.net.URI;
import java.net.URISyntaxException;

import static software.amazon.awssdk.regions.Region.EU_CENTRAL_1;

@Configuration
@EnableConfigurationProperties(DynamoProperties.class)
@Beta
public class DynamoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "dynamoClient", value = DynamoDBClient.class)
    DynamoDBClient dynamoClient(final DynamoProperties dynamoProperties) throws URISyntaxException {
        return DynamoDBClient.builder()
                .endpointOverride(new URI(dynamoProperties.getEndpoint()))
                .region(EU_CENTRAL_1)
                .credentialsProvider(new StaticCredentialsProvider(new AwsCredentials(dynamoProperties.getAccessKeyId(), dynamoProperties.getSecretKeyId())))
                .build();
    }
}
