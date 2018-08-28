package de.otto.edison.aws.s3.togglz;

import de.otto.edison.togglz.FeatureClassProvider;
import org.togglz.core.Feature;

public class TestFeatureClassProvider implements FeatureClassProvider {
    @Override
    public Class<? extends Feature> getFeatureClass() {
        return TestFeatures.class;
    }
}
