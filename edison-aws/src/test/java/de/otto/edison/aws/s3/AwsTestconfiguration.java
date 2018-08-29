package de.otto.edison.aws.s3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;

@Configuration
public class AwsTestconfiguration {

    @Bean
    @Profile("test")
    public AwsCredentialsProvider awsCredentialsProvider() {
        return AwsCredentialsProviderChain
                .builder()
                .credentialsProviders(AnonymousCredentialsProvider.create())
                .build();
    }
}
