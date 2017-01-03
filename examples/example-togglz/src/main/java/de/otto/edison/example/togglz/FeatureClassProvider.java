package de.otto.edison.example.togglz;

import org.springframework.stereotype.Component;
import org.togglz.core.Feature;

@Component
public class FeatureClassProvider implements de.otto.edison.togglz.FeatureClassProvider {
    @Override
    public Class<? extends Feature> getFeatureClass() {
        return Features.class;
    }
}
