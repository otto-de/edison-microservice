package de.otto.edison.example.togglz;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum Features implements Feature {

    @Label("Toggles the 'Hello Edison' message displayed on http://localhost/8080/example page")
    HELLO_TOGGLE;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
