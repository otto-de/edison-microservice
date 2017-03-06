package de.otto.edison.mongo.configuration;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

import com.mongodb.client.MongoDatabase;

import de.otto.edison.mongo.togglz.MongoTogglzRepository;
import de.otto.edison.togglz.FeatureClassProvider;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.togglz.configuration.TogglzConfiguration")
public class MongoTogglzConfiguration {

    private static final Logger LOG = getLogger(MongoTogglzConfiguration.class);

    @Bean
    StateRepository stateRepository(final MongoDatabase mongoDatabase, final FeatureClassProvider featureClassProvider,
                                    final UserProvider userProvider) {
        LOG.info("===============================");
        LOG.info("Using MongoTogglzRepository with " + mongoDatabase.getClass().getSimpleName() + " MongoDatabase impl.");
        LOG.info("===============================");
        return new MongoTogglzRepository(mongoDatabase, featureClassProvider, userProvider);
    }
}
