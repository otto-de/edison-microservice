package de.otto.edison.oauth.configuration;

import de.otto.edison.oauth.KeyExchangeJwtAccessTokenConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.annotation.Resource;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Resource
    private KeyExchangeJwtAccessTokenConverter keyExchangeJwtAccessTokenConverter;

    @Value("${edison.oauth.jwt.audience}")
    private String audience;

    @Value("${edison.oauth.authorization.resource.patterns:/oauth2/**}")
    private String[] resourceServerUrlPatterns;

    @Override
    public void configure(final ResourceServerSecurityConfigurer config) {
        config.tokenServices(tokenServices()).resourceId(audience);
    }

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http
                .headers().disable()
                .csrf().disable()
                .authorizeRequests().antMatchers(resourceServerUrlPatterns).authenticated();
    }

    private JwtAccessTokenConverter accessTokenConverter() {
        return keyExchangeJwtAccessTokenConverter;
    }

    private TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    private DefaultTokenServices tokenServices() {
        final DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        return defaultTokenServices;
    }
}
