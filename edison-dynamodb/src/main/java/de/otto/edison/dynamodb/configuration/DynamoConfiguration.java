package de.otto.edison.dynamodb.configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import de.otto.edison.annotations.Beta;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import static com.amazonaws.regions.Regions.EU_CENTRAL_1;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableConfigurationProperties(DynamoProperties.class)
@Beta
public class DynamoConfiguration {

    private static final Logger LOG = getLogger(DynamoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(name = "dynamoClient", value = AmazonDynamoDB.class)
    AmazonDynamoDB dynamoClient(final DynamoProperties dynamoProperties, final AWSCredentialsProvider credentialsProvider) {
        LOG.info("Creating DynamoClient");
        final EndpointConfiguration endpointConfiguration = new EndpointConfiguration(dynamoProperties.getEndpoint(), EU_CENTRAL_1.getName());
        return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(credentialsProvider)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(name = "dynamoDatabase", value = DynamoDB.class)
    DynamoDB dynamoDatabase(final AmazonDynamoDB dynamoClient) {
        return new DynamoDB(dynamoClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "credentialsProvider", value = AWSCredentialsProvider.class)
    AWSCredentialsProvider credentialsProvider(final DynamoProperties dynamoProperties) {
        final String profileName = dynamoProperties.getProfileName();
        if ("instance".equals(profileName)) {
            return InstanceProfileCredentialsProvider.getInstance();
        } else if ("test".equals(profileName)) {
            return new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test"));
        }
        return new ProfileCredentialsProvider(profileName);
    }
}
