package de.otto.edison.togglz.controller;

import de.otto.edison.togglz.EmptyFeatures;
import de.otto.edison.togglz.TestFeatures;
import org.junit.Test;

import java.util.Map;

import static de.otto.edison.togglz.controller.FeatureTogglesRepresentation.togglzRepresentation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class FeatureTogglesRepresentationTest {

    private FeatureTogglesRepresentation testee;

    @Test
    public void testGetFeatureRepresentation() {
        testee = togglzRepresentation(() -> TestFeatures.class);

        final Map<String, FeatureToggleRepresentation> features = testee.features;
        assertThat(features.get("TEST_FEATURE"), is(new FeatureToggleRepresentation("a test feature toggle", true, null)));
    }

    @Test
    public void testGetEmptyFeatureRepresentation() {
        testee = togglzRepresentation(() -> EmptyFeatures.class);

        final Map<String, FeatureToggleRepresentation> features = testee.features;
        assertThat(features, is(notNullValue()));
        assertThat(features.isEmpty(), is(true));
    }
}
