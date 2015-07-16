package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.DefaultTogglzConfig;
import de.otto.edison.togglz.FeatureClassProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class TogglzConfiguration {

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private FeatureClassProvider featureClassProvider;



    @Bean
    @ConditionalOnMissingBean(UserProvider.class)
    public UserProvider getUserProvider() {
        return () -> {

            HttpServletRequest request = HttpServletRequestHolder.get();

            String username = (String) request.getAttribute("username");
            boolean isAdmin = true; // "admin".equals(username);

            return new SimpleFeatureUser(username, isAdmin);

        };
    }

    @Value("${edison.togglz.cache.ttlMilliseconds:5000}")
    private long cacheTtlMilliseconds;

    @Bean
    public DefaultTogglzConfig defaultTogglzConfig() {
        return new DefaultTogglzConfig(cacheTtlMilliseconds, stateRepository, getUserProvider(), featureClassProvider);
    }


}
