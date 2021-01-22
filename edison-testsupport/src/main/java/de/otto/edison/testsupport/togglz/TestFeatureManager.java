package de.otto.edison.testsupport.togglz;


import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.metadata.FeatureMetaData;
import org.togglz.core.metadata.enums.EnumFeatureMetaData;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.util.Validate;

import java.util.*;

import static java.util.Collections.emptyList;

/**
 * A {@link FeatureManager} implementation that allows easy manipulation of features in testing environments.
 */
public class TestFeatureManager implements FeatureManager {

    private final Class<? extends Feature> featureEnum;

    private final Map<String, FeatureState> featureStates = new HashMap<>();

    public TestFeatureManager(Class<? extends Feature> featureEnum) {
        Validate.notNull(featureEnum, "The featureEnum argument is required");
        Validate.isTrue(featureEnum.isEnum(), "This feature manager currently only works with feature enums");
        this.featureEnum = featureEnum;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName() + ":" + featureEnum.getSimpleName();
    }

    @Override
    public Set<Feature> getFeatures() {
        return new HashSet<Feature>(Arrays.asList(featureEnum.getEnumConstants()));
    }

    @Override
    public FeatureMetaData getMetaData(Feature feature) {
        return new EnumFeatureMetaData(feature);
    }

    @Override
    public boolean isActive(Feature feature) {
        if (featureStates.containsKey(feature.name())) {
            return featureStates.get(feature.name()).isEnabled();
        }
        return false;
    }

    @Override
    public FeatureUser getCurrentFeatureUser() {
        boolean featureAdmin = true;
        return new SimpleFeatureUser("p13n-testing-user", featureAdmin);
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        if(featureStates.containsKey(feature.name())){
            return featureStates.get(feature.name());
        }
        return new FeatureState(feature, false);
    }

    @Override
    public void setFeatureState(FeatureState state) {
        if (state.getFeature() == null) {
            throw new RuntimeException("missing feature in state");
        }
        featureStates.put(state.getFeature().name(), state);
    }

    @Override
    public List<ActivationStrategy> getActivationStrategies() {
        return emptyList();
    }

    public TestFeatureManager setEnabled(Feature feature, boolean enabled) {
        if (featureStates.containsKey(feature.name())) {
            featureStates.get(feature.name()).setEnabled(enabled);
        } else {
            featureStates.put(feature.name(), new FeatureState(feature, false));
        }
        return this;
    }

    public TestFeatureManager enable(Feature feature) {
        return setEnabled(feature, true);
    }

    public TestFeatureManager disable(Feature feature) {
        return setEnabled(feature, false);
    }

    public TestFeatureManager enableAll() {
        for (FeatureState featureState : featureStates.values()) {
            featureState.setEnabled(false);
        }
        return this;
    }

    public TestFeatureManager disableAll() {
        for (FeatureState featureState : featureStates.values()) {
            featureState.setEnabled(false);
        }
        return this;
    }

}
