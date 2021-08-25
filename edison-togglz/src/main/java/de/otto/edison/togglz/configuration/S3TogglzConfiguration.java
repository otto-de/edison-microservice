package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.DefaultTogglzConfig;
import de.otto.edison.togglz.FeatureClassProvider;
import de.otto.edison.togglz.RemoteTogglzConfig;
import de.otto.edison.togglz.s3.FeatureStateConverter;
import de.otto.edison.togglz.s3.S3TogglzRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;

@Configuration
@EnableConfigurationProperties(TogglzProperties.class)
@ConditionalOnProperty(prefix = "edison.togglz", name = "s3.enabled", havingValue = "true")
@ConditionalOnBean(type = "software.amazon.awssdk.services.s3.S3Client")
public class S3TogglzConfiguration implements RemoteTogglzConfig {

    private static final Logger LOG = LoggerFactory.getLogger(S3TogglzConfiguration.class);

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
        LOG.info("========================");
        LOG.info("Using S3TogglzRepository");
        LOG.info("========================");
        return new S3TogglzRepository(featureStateConverter);
    }

    @Bean
    @ConditionalOnProperty(name = "edison.togglz.s3.check-bucket", havingValue = "true")
    public Boolean checkBucketAvailability(final S3Client s3Client, final TogglzProperties togglzProperties){

        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder()
                .bucket(togglzProperties.getS3().getBucketName())
                .build();
        //throws exception on missing bucket
        s3Client.listObjects(listObjectsRequest);

        return true;
    }
}
