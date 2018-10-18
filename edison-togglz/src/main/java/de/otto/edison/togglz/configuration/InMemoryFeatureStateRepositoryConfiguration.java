package de.otto.edison.togglz.configuration;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
public class InMemoryFeatureStateRepositoryConfiguration {

    private static final Logger LOG = getLogger(InMemoryFeatureStateRepositoryConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(StateRepository.class)
    public StateRepository stateRepository() {
        LOG.warn("===============================");
        LOG.warn("Using in-memory StateRepository for feature toggles");
        LOG.warn("===============================");
        return new InMemoryStateRepository();
    }
}
