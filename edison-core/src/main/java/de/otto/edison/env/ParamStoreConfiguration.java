package de.otto.edison.env;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.ssm.SsmClient;

@AutoConfiguration
@EnableConfigurationProperties(ParamStoreProperties.class)
@ConditionalOnProperty(name = "edison.env.paramstore.enabled", havingValue = "true")
public class ParamStoreConfiguration {

    @Bean
    public static ParamStorePropertySourcePostProcessor paramStorePropertySourcePostProcessor(final SsmClient ssmClient) {
        return new ParamStorePropertySourcePostProcessor(ssmClient);
    }
}
