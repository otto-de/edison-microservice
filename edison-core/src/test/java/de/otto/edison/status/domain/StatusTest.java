package de.otto.edison.status.domain;

import org.junit.Test;

import static de.otto.edison.status.domain.Status.ERROR;
import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;
import static de.otto.edison.status.domain.Status.plus;
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
