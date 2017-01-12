package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.DefaultTogglzConfig;
import de.otto.edison.togglz.FeatureClassProvider;
import de.otto.edison.togglz.authentication.Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.servlet.TogglzFilter;
import org.togglz.servlet.util.HttpServletRequestHolder;
import org.togglz.spring.manager.FeatureManagerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Configuration
@EnableConfigurationProperties(TogglzProperties.class)
public class TogglzConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "togglzFilter")
    public FilterRegistrationBean togglzFilter() {
        final FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new TogglzFilter());
        filterRegistration.addUrlPatterns("/*");
        return filterRegistration;
    }

    @Bean
    @ConditionalOnMissingBean(FeatureClassProvider.class)
    public FeatureClassProvider featureClassProvider() {
        return () -> Features.class;
    }

    @Bean
    @ConditionalOnMissingBean(UserProvider.class)
    public UserProvider userProvider() {
        return () -> {

            HttpServletRequest request = HttpServletRequestHolder.get();

            Optional<Credentials> credentials = Credentials.readFrom(request);
            boolean isAdmin = true; // "admin".equals(username);

            return new SimpleFeatureUser((credentials.isPresent() ? credentials.get().getUsername() : null), isAdmin);
        };
    }

    @Bean
    @Autowired
    public TogglzConfig togglzConfig(final StateRepository stateRepository,
                                     final FeatureClassProvider featureClassProvider,
                                     final TogglzProperties togglzProperties) {
        return new DefaultTogglzConfig(togglzProperties, stateRepository, userProvider(), featureClassProvider);
    }

    @Bean
    public FeatureManager featureManager(final TogglzConfig togglzConfig) throws Exception {
        FeatureManagerFactory featureManagerFactory = new FeatureManagerFactory();
        featureManagerFactory.setTogglzConfig(togglzConfig);
        FeatureManager featureManager = featureManagerFactory.getObject();
        StaticFeatureManagerProvider.setFeatureManager(featureManager);  // this workaround should be fixed with togglz version 2.2
        return featureManager;
    }

    private enum Features implements Feature {
        /* no features */
    }
}
