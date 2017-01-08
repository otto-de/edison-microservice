package de.otto.edison.togglz;

import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;

public enum EmptyFeatures implements Feature {

    ;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
