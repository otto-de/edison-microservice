package de.otto.edison.oauth.configuration;

import de.otto.edison.oauth.OAuthPublicKeyInMemoryRepository;
import de.otto.edison.oauth.OAuthPublicKeyRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuthPublicKeyConfiguration {

    @Bean
    @ConditionalOnMissingBean(OAuthPublicKeyRepository.class)
    public OAuthPublicKeyRepository inMemoryRepository() {
        return new OAuthPublicKeyInMemoryRepository();
    }
}
