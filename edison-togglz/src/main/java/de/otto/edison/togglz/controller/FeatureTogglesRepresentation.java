package de.otto.edison.togglz.controller;

import de.otto.edison.togglz.FeatureClassProvider;
import net.jcip.annotations.Immutable;
import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;

import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static org.togglz.core.context.FeatureContext.getFeatureManager;

@Immutable
public class FeatureTogglesRepresentation {

    public final Map<String, FeatureToggleRepresentation> features;

    private FeatureTogglesRepresentation(final Class<? extends Feature> featureClass) {
        this.features = buildTogglzState(featureClass);
    }

    public static FeatureTogglesRepresentation togglzRepresentation(final FeatureClassProvider featureClassProvider) {
        return new FeatureTogglesRepresentation(featureClassProvider.getFeatureClass());
    }

    private Map<String, FeatureToggleRepresentation> buildTogglzState(final Class<? extends Feature> featureClass) {
        final Feature[] features = featureClass.getEnumConstants();
        return stream(features)
                .collect(
                        toMap(Feature::name, this::toFeatureToggleRepresentation)
                );
    }

    private FeatureToggleRepresentation toFeatureToggleRepresentation(final Feature feature) {
        final Label label = getLabelAnnotation(feature);
        return new FeatureToggleRepresentation(
                label != null ? label.value() : feature.name(),
                getFeatureManager().getFeatureState(feature).isEnabled(),
                null);
    }

    public static Label getLabelAnnotation(Feature feature) {
        try {
            Class<? extends Feature> featureClass = feature.getClass();
            Label fieldAnnotation = featureClass.getField(feature.name()).getAnnotation(Label.class);
            Label classAnnotation = featureClass.getAnnotation(Label.class);

            return fieldAnnotation != null ? fieldAnnotation : classAnnotation;
        } catch (SecurityException e) {
            // ignore
        } catch (NoSuchFieldException e) {
            // ignore
        }
        return null;
    }

}
