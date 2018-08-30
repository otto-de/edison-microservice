package de.otto.edison.testsupport.aws;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;

@Configuration
@ConditionalOnClass(name = "de.otto.edison.aws.configuration.AwsConfiguration")
public class AwsTestconfiguration {

    @Bean
    @Profile("test")
    public AwsCredentialsProvider awsCredentialsProvider() {
        final AnonymousCredentialsProvider anonymousCredentialsProvider = AnonymousCredentialsProvider.create();
        return AwsCredentialsProviderChain
                .builder()
                .credentialsProviders(anonymousCredentialsProvider)
                .build();
    }
}
