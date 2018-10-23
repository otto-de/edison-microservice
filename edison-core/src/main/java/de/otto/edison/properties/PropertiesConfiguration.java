package de.otto.edison.properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.ssm.SsmClient;

@Configuration
@ConditionalOnProperty(name = "edison.aws.config.paramstore.enabled", havingValue = "true")
@ConditionalOnBean(type = "software.amazon.awssdk.services.ssm.SsmClient")
public class PropertiesConfiguration {

    @Bean
    public ParamStorePropertySourcePostProcessor paramStorePropertySourcePostProcessor(final SsmClient ssmClient) {
        return new ParamStorePropertySourcePostProcessor(ssmClient);
    }
}
