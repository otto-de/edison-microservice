package de.otto.edison.dynamodb.configuration;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;

import de.otto.edison.annotations.Beta;
import de.otto.edison.dynamodb.togglz.DynamoTogglzRepository;
import de.otto.edison.togglz.FeatureClassProvider;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.togglz.configuration.TogglzConfiguration")
@Beta
public class DynamoTogglzConfiguration {

    private static final Logger LOG = getLogger(DynamoTogglzConfiguration.class);

    @Bean
    StateRepository stateRepository(final AmazonDynamoDB dynamoDB, final FeatureClassProvider featureClassProvider,
                                    final UserProvider userProvider) {
        LOG.info("===============================");
        LOG.info("Using DynamoTogglzRepository with " + dynamoDB.getClass().getSimpleName() + " MongoDatabase impl.");
        LOG.info("===============================");
        return new DynamoTogglzRepository(dynamoDB, featureClassProvider, userProvider);
    }
}
