package de.otto.edison.togglz;

import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.user.UserProvider;

public class DefaultTogglzConfig implements TogglzConfig {

    private StateRepository cachingStateRepository;
    private UserProvider userProvider;
    private FeatureClassProvider featureClassProvider;

    public DefaultTogglzConfig(final long ttlMilliseconds,
                               final StateRepository stateRepository,
                               final UserProvider userProvider,
                               final FeatureClassProvider featureClassProvider) {
        this.cachingStateRepository = new CachingStateRepository(stateRepository, ttlMilliseconds);
        this.userProvider = userProvider;
        this.featureClassProvider = featureClassProvider;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return featureClassProvider.getFeatureClass();
    }

    @Override
    public StateRepository getStateRepository() {
        return cachingStateRepository;
    }

    @Override
    public UserProvider getUserProvider() {
       return userProvider;
    }
}
