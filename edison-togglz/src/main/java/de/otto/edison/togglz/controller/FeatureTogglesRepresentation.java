package de.otto.edison.togglz.controller;

import net.jcip.annotations.Immutable;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.metadata.enums.AnnotationFeatureGroup;
import org.togglz.core.repository.FeatureState;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

@Immutable
public class FeatureTogglesRepresentation {

    public final Map<String, FeatureToggleRepresentation> features;

    private FeatureTogglesRepresentation(final FeatureManager featureManager) {
        this.features = buildTogglzState(featureManager);
    }

    public static FeatureTogglesRepresentation togglzRepresentation(final FeatureManager featureManager) {
        return new FeatureTogglesRepresentation(featureManager);
    }

    private Map<String, FeatureToggleRepresentation> buildTogglzState(final FeatureManager featureManager) {
        Feature[] features = featureManager.getFeatures().toArray(new Feature[]{});
        return stream(features)
                .collect(
                        toMap(Feature::name, feature -> toFeatureToggleRepresentation(feature, featureManager))
                );
    }

    private FeatureToggleRepresentation toFeatureToggleRepresentation(final Feature feature, FeatureManager featureManager) {

        final String label = featureManager.getMetaData(feature).getLabel();
        final List<String> featureGroups = featureManager.getMetaData(feature).getGroups().stream()
                .map(FeatureGroup::getLabel)
                .collect(Collectors.toList());
        FeatureState featureState = featureManager.getFeatureState(feature);
        return FeatureToggleRepresentation.newBuilder()
                .withDescription(label != null ? label : feature.name())
                .withGroups(featureGroups)
                .withEnabled(featureState.isEnabled())
                .withStrategy(featureState.getStrategyId())
                .build();
    }
}
