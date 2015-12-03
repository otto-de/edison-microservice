package de.otto.edison.togglz.controller;

import de.otto.edison.togglz.TestFeatures;
import org.testng.annotations.Test;

import java.util.Map;

import static de.otto.edison.togglz.controller.TogglzRepresentation.togglzRepresentation;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Test
public class TogglzRepresentationTest {

    private TogglzRepresentation testee;

    @Test
    public void testGetTogglzState() {
        testee = togglzRepresentation(() -> TestFeatures.class);

        final Map<String, Boolean> togglzState = testee.getTogglzState();
        assertThat(togglzState.get("TEST_FEATURE"), is(true));
    }
}
