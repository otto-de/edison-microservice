package de.otto.edison.togglz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

@Component
public class DefaultTogglzConfig implements TogglzConfig {

	private final StateRepository stateRepository;
    private final UserProvider userProvider;
    private FeatureClassProvider featureClassProvider;

    @Autowired
    public DefaultTogglzConfig(final StateRepository stateRepository,
                               final UserProvider userProvider,
                               final FeatureClassProvider featureClassProvider) {
        this.stateRepository = stateRepository;
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