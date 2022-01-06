package de.otto.edison.togglz.util;

import org.togglz.core.activation.Parameter;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.FeatureUser;
import org.togglz.servlet.activation.HeaderActivationStrategy;

public class HeaderOptOutStrategy extends HeaderActivationStrategy {
    static final String ID = "headerOptOut";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "HeaderOptOut";
    }

    @Override
    public boolean isActive(FeatureState featureState, FeatureUser user) {
        return !super.isActive(featureState, user);
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }
}
