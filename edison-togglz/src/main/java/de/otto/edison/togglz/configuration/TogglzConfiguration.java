package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.DefaultTogglzConfig;
import de.otto.edison.togglz.FeatureClassProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.servlet.util.HttpServletRequestHolder;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class TogglzConfiguration {

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private FeatureClassProvider featureClassProvider;

    @Bean
    @ConditionalOnMissingBean(FeatureClassProvider.class)
    public FeatureClassProvider getFeatureClassProvider() {
        return () -> Features.class;
    }

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

    static enum Features implements Feature {
        NONE;
        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }
    }
}
