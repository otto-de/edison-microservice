package de.otto.edison.togglz;

import org.togglz.core.Feature;

public interface FeatureClassProvider {
    Class<? extends Feature> getFeatureClass();
}
