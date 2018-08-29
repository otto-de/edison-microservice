package de.otto.edison.aws.configuration;

import de.otto.edison.aws.s3.S3Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ssm.SsmClient;

import static software.amazon.awssdk.regions.Region.of;

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
public class AwsConfiguration {

    @Bean
    @ConditionalOnMissingBean(AwsCredentialsProvider.class)
    @Profile({"prod", "live", "local", "develop"})
    public AwsCredentialsProvider awsCredentialsProvider(final AwsProperties awsProperties) {
        return AwsCredentialsProviderChain
                .builder()
                .credentialsProviders(
                        ContainerCredentialsProvider.builder().build(),
                        InstanceProfileCredentialsProvider.builder().build(),
                        EnvironmentVariableCredentialsProvider.create(),
                        ProfileCredentialsProvider
                                .builder()
                                .profileName(awsProperties.getProfile())
                                .build())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(S3Client.class)
    public S3Client s3Client(final AwsProperties awsProperties,
                             final AwsCredentialsProvider awsCredentialsProvider) {
        return S3Client
                .builder()
                .region(of(awsProperties.getRegion()))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Service s3Service(final S3Client s3Client) {
        return new S3Service(s3Client);
    }
}
