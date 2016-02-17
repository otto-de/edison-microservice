package de.otto.edison.togglz.controller;

import de.otto.edison.togglz.FeatureClassProvider;
import net.jcip.annotations.Immutable;
import org.togglz.core.Feature;

import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.togglz.core.context.FeatureContext.getFeatureManager;

@Immutable
public class TogglzRepresentation {

    private final Map<String, Boolean> togglzState;

    private TogglzRepresentation(final Class<Feature> featureClass) {
        this.togglzState = buildTogglzState(featureClass);
    }

    public static TogglzRepresentation togglzRepresentation(final FeatureClassProvider featureClassProvider) {
        return new TogglzRepresentation((Class<Feature>) featureClassProvider.getFeatureClass());
    }

    private Map<String, Boolean> buildTogglzState(final Class<Feature> featureClass) {
        return asList(featureClass.getEnumConstants()).stream()
                .collect(toMap(this::toggleName, this::toggleState));
    }

    private boolean toggleState(Feature feature) {
        return getFeatureManager().getFeatureState(feature).isEnabled();
    }

    private String toggleName(Feature features) {
        return features.name();
    }

    public Map<String, Boolean> getTogglzState() {
        return togglzState;
    }
}
