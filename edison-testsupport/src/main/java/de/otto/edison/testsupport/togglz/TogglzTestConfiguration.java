package de.otto.edison.testsupport.togglz;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.togglz.core.user.FeatureUser;
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

}
