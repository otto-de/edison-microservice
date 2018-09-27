package de.otto.edison.togglz.s3;

import de.otto.edison.togglz.DefaultTogglzConfig;
import de.otto.edison.togglz.FeatureClassProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

@Configuration
@EnableConfigurationProperties(S3TogglzProperties.class)
@ConditionalOnProperty(name = "edison.togglz.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(name = "org.togglz.core.repository.StateRepository")
public class S3TogglzConfiguration {

    @Bean
    @ConditionalOnProperty(name = "edison.togglz.s3.bucket-name")
    public TogglzConfig togglzConfig(final StateRepository stateRepository,
                                     final FeatureClassProvider featureClassProvider,
                                     final UserProvider userProvider) {
        return new DefaultTogglzConfig(stateRepository, userProvider, featureClassProvider);
    }

    @Bean
    @ConditionalOnProperty(name = "edison.togglz.s3.bucket-name")
    public StateRepository stateRepository(final FeatureStateConverter featureStateConverter) {
        return new S3TogglzRepository(featureStateConverter);
    }
}
