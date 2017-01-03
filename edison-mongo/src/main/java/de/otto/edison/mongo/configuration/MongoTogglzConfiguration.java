package de.otto.edison.mongo.configuration;

import com.mongodb.client.MongoDatabase;
import de.otto.edison.mongo.togglz.MongoFeatureRepository;
import de.otto.edison.togglz.FeatureClassProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.togglz.configuration.TogglzConfiguration")
public class MongoTogglzConfiguration {

    @Bean
    StateRepository stateRepository(final MongoDatabase database,
                                    final FeatureClassProvider featureClassProvider,
                                    final UserProvider userProvider) {
        return new MongoFeatureRepository(database, featureClassProvider, userProvider);
    }
}
