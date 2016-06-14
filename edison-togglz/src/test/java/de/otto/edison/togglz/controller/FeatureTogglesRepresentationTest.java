package de.otto.edison.togglz.controller;

import de.otto.edison.togglz.TestFeatures;
import org.testng.annotations.Test;

import java.util.Map;

import static de.otto.edison.togglz.controller.FeatureTogglesRepresentation.togglzRepresentation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Test
public class FeatureTogglesRepresentationTest {

    private FeatureTogglesRepresentation testee;

    @Test
    public void testGetTogglzState() {
        testee = togglzRepresentation(() -> TestFeatures.class);

        final Map<String, FeatureToggleRepresentation> togglzState = testee.features;
        assertThat(togglzState.get("TEST_FEATURE"), is(new FeatureToggleRepresentation("a test feature toggle", true, null)));
    }
}
