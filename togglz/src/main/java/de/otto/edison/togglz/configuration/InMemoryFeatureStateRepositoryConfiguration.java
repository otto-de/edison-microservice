package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.FeatureClassProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class InMemoryFeatureStateRepositoryConfiguration {

    @Bean
    @ConditionalOnMissingBean(FeatureClassProvider.class)
    public FeatureClassProvider features() {
        return () -> Features.class;
    }

    @Bean
    @ConditionalOnMissingBean(StateRepository.class)
    public StateRepository stateRepository() {
        return createInMemoryStateRepository();
    }


    private StateRepository createInMemoryStateRepository() {
        return new StateRepository() {

            Logger LOG = LoggerFactory.getLogger(TogglzConfiguration.class);

            private Map<String, FeatureState> featureStore = new HashMap<>();

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
                LOG.info("Switched feature state to " + featureState.toString());
            }
        };
    }



    public enum Features implements Feature {

        @Label("test")
        TEST;

        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }
    }
}
