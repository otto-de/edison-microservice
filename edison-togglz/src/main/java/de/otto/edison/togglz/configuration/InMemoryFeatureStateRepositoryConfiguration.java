package de.otto.edison.togglz.configuration;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

import java.util.HashMap;
import java.util.Map;

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
        return createInMemoryStateRepository();
    }


    private StateRepository createInMemoryStateRepository() {
        return new StateRepository() {

            Logger LOG = getLogger(TogglzConfiguration.class);

            private Map<String, FeatureState> featureStore = new HashMap<>();

            @Autowired
            UserProvider userProvider;

            @Override
            public FeatureState getFeatureState(final Feature feature) {
                if (featureStore.containsKey(feature.name())) {
                    return featureStore.get(feature.name());
                }
                return new FeatureState(feature, false);
            }

            @Override
            public void setFeatureState(final FeatureState featureState) {
                featureStore.put(featureState.getFeature().name(), featureState);
                LOG.info((!StringUtils.isEmpty(userProvider.getCurrentUser().getName()) ? "User '" + userProvider.getCurrentUser().getName() + "'" : "Unknown user")
                        + (featureState.isEnabled() ? " enabled " : " disabled ") + "feature " + featureState.getFeature().name());
            }
        };
    }

}
