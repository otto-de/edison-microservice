package de.otto.edison.togglz.controller;

import de.otto.edison.togglz.TestFeatures;
import org.testng.annotations.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Test
public class TogglzInformationControllerTest {

    private TogglzInformationController testee;

    @Test
    public void shouldReturnTogglzRepresentation() {
        testee = new TogglzInformationController(() -> TestFeatures.class);

        final Map<String, Boolean> togglzState = testee.getStatusAsJson().getTogglzState();
        assertThat(togglzState.get("TEST_FEATURE"), is(true));
    }
}
