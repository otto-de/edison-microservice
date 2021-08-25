package de.otto.edison.togglz.configuration;

import de.otto.edison.authentication.Credentials;
import de.otto.edison.togglz.DefaultTogglzConfig;
import de.otto.edison.togglz.FeatureClassProvider;
import de.otto.edison.togglz.RemoteTogglzConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.core.Feature;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;
import org.togglz.servlet.TogglzFilter;
import org.togglz.servlet.util.HttpServletRequestHolder;
import org.togglz.spring.manager.FeatureManagerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableConfigurationProperties(TogglzProperties.class)
public class TogglzConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "togglzFilter")
    public FilterRegistrationBean<TogglzFilter> togglzFilter() {
        final FilterRegistrationBean<TogglzFilter> filterRegistration = new FilterRegistrationBean<>();
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

            final HttpServletRequest request = HttpServletRequestHolder.get();

            final Optional<Credentials> credentials = Credentials.readFrom(request);
            final boolean isAdmin = true; // "admin".equals(username);

            return new SimpleFeatureUser((credentials.isPresent() ? credentials.get().getUsername() : null), isAdmin);
        };
    }

    @Bean
    public Boolean validateRemoteConfigs(ApplicationContext applicationContext) {
        Map<String, RemoteTogglzConfig> remoteToggleConfigs = applicationContext.getBeansOfType(RemoteTogglzConfig.class);

        if (remoteToggleConfigs.size() > 1) {
            String names = String.join(",", remoteToggleConfigs.keySet());
            throw new RuntimeException("multiple remote togglz configs exist, make sure only one of them is used: " + names);
        }

        return true;
    }

    @Bean
    @ConditionalOnMissingBean(TogglzConfig.class)
    public TogglzConfig togglzConfig(final StateRepository stateRepository,
                                     final FeatureClassProvider featureClassProvider,
                                     final TogglzProperties togglzProperties,
                                     final UserProvider userProvider) {
        final CachingStateRepository cachingStateRepository = new CachingStateRepository(stateRepository, togglzProperties.getCacheTtl());
        return new DefaultTogglzConfig(cachingStateRepository, userProvider, featureClassProvider);
    }

    @Bean
    @ConditionalOnMissingBean(FeatureManager.class)
    public FeatureManager featureManager(final TogglzConfig togglzConfig) throws Exception {
        final FeatureManagerFactory featureManagerFactory = new FeatureManagerFactory();
        featureManagerFactory.setTogglzConfig(togglzConfig);
        final FeatureManager featureManager = featureManagerFactory.getObject();
        StaticFeatureManagerProvider.setFeatureManager(featureManager);  // this workaround should be fixed with togglz version 2.2
        return featureManager;
    }

    private enum Features implements Feature {
        /* no features */
    }
}
