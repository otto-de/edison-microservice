package de.otto.edison.aws.configuration;

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
import software.amazon.awssdk.services.ssm.SsmClient;

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
    public SsmClient ssmClient(final AwsCredentialsProvider awsCredentialsProvider) {
        return SsmClient.builder()
                .credentialsProvider(awsCredentialsProvider)
                .build();

    }
}
