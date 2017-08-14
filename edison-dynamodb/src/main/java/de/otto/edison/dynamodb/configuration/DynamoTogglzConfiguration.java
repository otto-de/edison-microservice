package de.otto.edison.dynamodb.configuration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import de.otto.edison.annotations.Beta;
import de.otto.edison.dynamodb.togglz.DynamoTogglzRepository;
import de.otto.edison.togglz.FeatureClassProvider;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.togglz.configuration.TogglzConfiguration")
@Beta
public class DynamoTogglzConfiguration {

    private static final Logger LOG = getLogger(DynamoTogglzConfiguration.class);

    @Bean
    StateRepository stateRepository(final AmazonDynamoDB dynamoClient, final DynamoDB dynamoDatabase, final FeatureClassProvider featureClassProvider, final UserProvider userProvider) {
        LOG.info("===============================");
        LOG.info("Using DynamoTogglzRepository with " + dynamoClient.getClass().getSimpleName() + " DynamoDatabase impl.");
        LOG.info("===============================");
        return new DynamoTogglzRepository(dynamoClient, dynamoDatabase, featureClassProvider, userProvider);
    }
}
