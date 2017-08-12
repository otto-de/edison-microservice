package de.otto.edison.dynamodb.togglz;

import org.springframework.stereotype.Component;
import org.togglz.core.Feature;

import de.otto.edison.togglz.FeatureClassProvider;

@Component
public class TestFeatureClassProvider implements FeatureClassProvider {
    @Override
    public Class<? extends Feature> getFeatureClass() {
        return TestFeatures.class;
    }
}
