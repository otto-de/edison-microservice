package de.otto.edison.oauth.configuration;

import de.otto.edison.oauth.OAuthPublicKeyInMemoryRepository;
import de.otto.edison.oauth.OAuthPublicKeyRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.net.http.HttpClient;

@Configuration
public class OAuthPublicKeyConfiguration  extends WebSecurityConfigurerAdapter {

    @Bean
    @Override
    @ConditionalOnMissingBean(AuthenticationManager.class)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    @ConditionalOnMissingBean(OAuthPublicKeyRepository.class)
    public OAuthPublicKeyRepository inMemoryRepository() {
        return new OAuthPublicKeyInMemoryRepository();
    }

    @Bean
    @ConditionalOnMissingBean(HttpClient.class)
    public HttpClient asyncHttpClient() {
        return HttpClient.newHttpClient();
    }

}
