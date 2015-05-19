package de.otto.edison.togglz;

import org.togglz.core.Feature;
/**
 * Implement this interface as a spring bean to expose Your Feature enum to the TogglzConfiguration.
 */
public interface FeatureClassProvider {
    Class<? extends Feature> getFeatureClass();
}
