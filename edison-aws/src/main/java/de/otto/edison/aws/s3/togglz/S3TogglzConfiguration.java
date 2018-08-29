package de.otto.edison.aws.s3.togglz;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3TogglzProperties.class)
@ConditionalOnProperty(name = "edison.aws.s3.togglz.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(name = "org.togglz.core.repository.StateRepository")
public class S3TogglzConfiguration {

    @Bean
    @ConditionalOnProperty(name = "edison.aws.s3.togglz.bucket-name")
    public StateRepository stateRepository(final S3TogglzProperties s3TogglzProperties,
                                           final S3Client s3Client) {
        final S3StateRepository togglzRepository = new S3StateRepository(s3TogglzProperties, s3Client);
        if (s3TogglzProperties.isPrefetch()) {
            return new PrefetchCachingStateRepository(togglzRepository);
        } else {
            return new CachingStateRepository(togglzRepository, s3TogglzProperties.getCacheTtl());
        }
    }
}
