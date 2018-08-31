package de.otto.edison.testsupport.togglz;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.togglz.configuration.TogglzConfiguration")
public class TogglzTestConfiguration {

    @Bean
    @Profile("test")
    public UserProvider userProvider() {
        return new UserProvider() {
            @Override
            public FeatureUser getCurrentUser() {
                return new SimpleFeatureUser("someName", false);
            }
        };
    }

    @Bean
    @Profile("test")
    public TogglzConfig togglzConfig() {
        return new TogglzConfig() {
            @Override
            public Class<? extends Feature> getFeatureClass() {
                return EmptyTestFeatures.class;
            }

            @Override
            public StateRepository getStateRepository() {
                return new InMemoryStateRepository();
            }

            @Override
            public UserProvider getUserProvider() {
                return new NoOpUserProvider();
            }
        };
    }

    public enum EmptyTestFeatures implements Feature {

        ;

        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }
    }

}
