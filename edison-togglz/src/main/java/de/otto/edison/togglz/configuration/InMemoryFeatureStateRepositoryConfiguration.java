package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.mongo.MongoTogglzRepository;
import de.otto.edison.togglz.s3.S3TogglzRepository;
import jakarta.annotation.Priority;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
@EnableConfigurationProperties(TogglzProperties.class)
@ConditionalOnMissingBean({MongoTogglzRepository.class, S3TogglzRepository.class})
public class InMemoryFeatureStateRepositoryConfiguration {

    private static final Logger LOG = getLogger(InMemoryFeatureStateRepositoryConfiguration.class);

    @Bean
    public StateRepository stateRepository() {
        LOG.warn("===============================");
        LOG.warn("Using in-memory StateRepository for feature toggles");
        LOG.warn("===============================");
        return new InMemoryStateRepository();
    }
}
