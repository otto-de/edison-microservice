package de.otto.edison.togglz;

import de.otto.edison.togglz.configuration.TogglzProperties;
import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.user.UserProvider;

public class DefaultTogglzConfig implements TogglzConfig {

    private StateRepository stateRepository;
    private UserProvider userProvider;
    private FeatureClassProvider featureClassProvider;

    public DefaultTogglzConfig(final TogglzProperties properties,
                               final StateRepository stateRepository,
                               final UserProvider userProvider,
                               final FeatureClassProvider featureClassProvider) {
        if (properties.getCacheTtl() > 0) {
            this.stateRepository = new CachingStateRepository(stateRepository, properties.getCacheTtl());
        } else {
            this.stateRepository = stateRepository;
        }
        this.userProvider = userProvider;
        this.featureClassProvider = featureClassProvider;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return featureClassProvider.getFeatureClass();
    }

    @Override
    public StateRepository getStateRepository() {
        return stateRepository;
    }

    @Override
    public UserProvider getUserProvider() {
       return userProvider;
    }
}
