package de.otto.µservice.status.domain;

import org.testng.annotations.Test;

import static de.otto.µservice.status.domain.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StatusTest {

    @Test
    public void shouldBeOk() {
        assertThat(plus(OK, OK), is(OK));
    }

    @Test
    public void shouldBeWarning() {
        assertThat(plus(OK, WARNING), is(WARNING));
    }

    @Test
    public void shouldBeError() {
        assertThat(plus(WARNING, ERROR), is(ERROR));
    }

    @Test
    public void shouldBeAbleToConcatenate() {
        assertThat(plus(OK, plus(WARNING, OK)), is(WARNING));
    }
}