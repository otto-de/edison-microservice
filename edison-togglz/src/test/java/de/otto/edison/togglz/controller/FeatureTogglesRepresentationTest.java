package de.otto.edison.togglz.controller;

import de.otto.edison.togglz.EmptyFeatures;
import de.otto.edison.togglz.TestFeatures;
import org.junit.jupiter.api.Test;
import org.togglz.core.manager.FeatureManager;
import org.togglz.testing.TestFeatureManager;

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
        assertThat(features.get("TEST_FEATURE"), is(new FeatureToggleRepresentation("a test feature toggle", false, null)));
    }

    @Test
    void testGetEmptyFeatureRepresentation() {
        FeatureManager featureManager = new TestFeatureManager(EmptyFeatures.class);
        testee = togglzRepresentation(featureManager);

        final Map<String, FeatureToggleRepresentation> features = testee.features;
        assertThat(features, is(notNullValue()));
        assertThat(features.isEmpty(), is(true));
    }
}
