package de.otto.edison.togglz.util;

import org.togglz.core.Feature;

import java.util.Optional;

import static org.togglz.core.context.FeatureContext.getFeatureManager;

public class FeatureManagerSupport {

    public static Optional<Feature> getFeatureFromName(String name) {
        return getFeatureManager().getFeatures().stream().filter(feature -> feature.name().equals(name)).findAny();
    }
}
