package de.otto.edison.togglz.controller;

import de.otto.edison.togglz.configuration.TogglzConfiguration;
import org.testng.annotations.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Test
public class TogglzInformationControllerTest {

    private TogglzInformationController testee;

    @Test
    public void shouldReturnTogglzRepresentation() {
        testee = new TogglzInformationController(() -> TogglzConfiguration.Features.class);

        final Map<String, Boolean> togglzState = testee.getStatusAsJson().getTogglzState();
        assertThat(togglzState.get("TEST"), is(true));
    }
}
