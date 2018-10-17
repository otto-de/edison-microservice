package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.DefaultTogglzConfig;
import de.otto.edison.togglz.FeatureClassProvider;
import de.otto.edison.togglz.s3.FeatureStateConverter;
import de.otto.edison.togglz.s3.S3TogglzRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(TogglzProperties.class)
@ConditionalOnProperty(name = "edison.togglz.s3.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(name = "software.amazon.awssdk.services.s3.S3Client")
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
    public FeatureStateConverter featureStateConverter(final S3Client s3Client, final TogglzProperties togglzProperties) {
        return new FeatureStateConverter(s3Client, togglzProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "edison.togglz.s3.bucket-name")
    public StateRepository stateRepository(final FeatureStateConverter featureStateConverter) {
        return new S3TogglzRepository(featureStateConverter);
    }
}
