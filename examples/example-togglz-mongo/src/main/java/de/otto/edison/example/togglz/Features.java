package de.otto.edison.example.togglz;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum Features implements Feature {

    // This is just a simple example on how you could formulate a feature toggle
    @Label("Describe how switching on/off TEST_TOGGLE changes behaviour ...")
    TEST_TOGGLE;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
