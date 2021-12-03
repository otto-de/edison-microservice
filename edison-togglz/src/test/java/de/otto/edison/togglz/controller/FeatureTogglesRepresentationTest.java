package de.otto.edison.togglz.controller;

import de.otto.edison.testsupport.togglz.TestFeatureManager;
import de.otto.edison.togglz.EmptyFeatures;
import de.otto.edison.togglz.TestFeatures;
import org.junit.jupiter.api.Test;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import java.util.List;
import java.util.Map;

import static de.otto.edison.togglz.controller.FeatureTogglesRepresentation.togglzRepresentation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class FeatureTogglesRepresentationTest {

    private FeatureTogglesRepresentation testee;

    @Test
    void testGetFeatureRepresentation() {
        FeatureManager featureManager = new TestFeatureManager(TestFeatures.class);
        testee = togglzRepresentation(featureManager);

        final Map<String, FeatureToggleRepresentation> features = testee.features;
        assertThat(features.get("TEST_FEATURE"), is(FeatureToggleRepresentation.newBuilder()
                .withDescription("a test feature toggle")
                .withEnabled(false)
                .build()));
    }

    @Test
    void testGetEmptyFeatureRepresentation() {
        FeatureManager featureManager = new TestFeatureManager(EmptyFeatures.class);
        testee = togglzRepresentation(featureManager);

        final Map<String, FeatureToggleRepresentation> features = testee.features;
        assertThat(features, is(notNullValue()));
        assertThat(features.isEmpty(), is(true));
    }

    @Test
    void shouldResolveFeatureGroups() {
        FeatureManager featureManager = new TestFeatureManager(TestFeatures.class);
        testee = togglzRepresentation(featureManager);

        final Map<String, FeatureToggleRepresentation> features = testee.features;
        assertThat(features.get("TEST_FEATURE_2"), is(FeatureToggleRepresentation.newBuilder()
                .withDescription("TEST_FEATURE_2")
                .withEnabled(false)
                .withGroups(List.of("TestToggleGroup"))
                .build()));
    }

    @Test
    void shouldResolveStrategy() {
        FeatureManager featureManager = new TestFeatureManager(TestFeatures.class);
        FeatureState state = new FeatureState(TestFeatures.TEST_FEATURE, true);
        state.setStrategyId("someStrategy");
        featureManager.setFeatureState(state);
        testee = togglzRepresentation(featureManager);

        final Map<String, FeatureToggleRepresentation> features = testee.features;
        assertThat(features.get("TEST_FEATURE").strategy, is("someStrategy"));
    }
}
