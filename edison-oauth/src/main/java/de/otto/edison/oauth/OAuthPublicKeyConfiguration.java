package de.otto.edison.oauth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "api.oauth.public-key", name = "enabled", havingValue = "true")
public class OAuthPublicKeyConfiguration {

    @Bean
    @ConditionalOnMissingBean(OAuthPublicKeyRepository.class)
    public OAuthPublicKeyRepository inMemoryRepository() {
        return new OAuthPublicKeyInMemoryRepository();
    }
}
