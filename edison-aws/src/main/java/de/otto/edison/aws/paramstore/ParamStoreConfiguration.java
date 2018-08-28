package de.otto.edison.aws.paramstore;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.ssm.SsmClient;

@Configuration
@EnableConfigurationProperties(ParamStoreProperties.class)
@ConditionalOnProperty(name = "edison.aws.config.paramstore.enabled", havingValue = "true")
public class ParamStoreConfiguration {

    @Bean
    public SsmClient awsSSM(AwsCredentialsProvider awsCredentialsProvider) {
        return SsmClient.builder()
            .credentialsProvider(awsCredentialsProvider)
            .build();
    }
}
