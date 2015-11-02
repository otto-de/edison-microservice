package de.otto.edison.togglz;

import org.springframework.stereotype.Component;
import org.togglz.core.Feature;

@Component
public class TestFeatureClassProvider implements FeatureClassProvider {
    @Override
    public Class<? extends Feature> getFeatureClass() {
        return TestFeatures.class;
    }
}
