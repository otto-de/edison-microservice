package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.StatusDetail;
import org.testng.annotations.Test;

import static de.otto.edison.status.domain.Status.ERROR;
import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;
import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public class MutableStatusDetailIndicatorTest {

    @Test
    public void shouldIndicateInitialStatus() {
        // given
        final MutableStatusDetailIndicator indicator = new MutableStatusDetailIndicator(statusDetail("foo", OK, "message"));
        // then
        assertThat(indicator.statusDetail().getName(), is("foo"));
        assertThat(indicator.statusDetail().getStatus(), is(OK));
        assertThat(indicator.statusDetail().getMessage(), is("message"));
    }

    @Test
    public void shouldIndicateUpdatedStatus() {
        // given
        final StatusDetail initial = statusDetail("foo", ERROR, "message");
        final MutableStatusDetailIndicator indicator = new MutableStatusDetailIndicator(initial);
        // when
        indicator.update(initial.toOk("ok now"));
        // then
        assertThat(indicator.statusDetail().getName(), is("foo"));
        assertThat(indicator.statusDetail().getMessage(), is("ok now"));
        assertThat(indicator.statusDetail().getStatus(), is(OK));
    }

    @Test
    public void shouldIndicateOkStatus() {
        // given
        final StatusDetail initial = statusDetail("foo", ERROR, "message");
        final MutableStatusDetailIndicator indicator = new MutableStatusDetailIndicator(initial);
        // when
        indicator.toOk("ok now");
        // then
        assertThat(indicator.statusDetail().getName(), is("foo"));
        assertThat(indicator.statusDetail().getMessage(), is("ok now"));
        assertThat(indicator.statusDetail().getStatus(), is(OK));
    }

    @Test
    public void shouldIndicateWarnStatus() {
        // given
        final StatusDetail initial = statusDetail("foo", ERROR, "message");
        final MutableStatusDetailIndicator indicator = new MutableStatusDetailIndicator(initial);
        // when
        indicator.toWarning("something strange");
        // then
        assertThat(indicator.statusDetail().getName(), is("foo"));
        assertThat(indicator.statusDetail().getMessage(), is("something strange"));
        assertThat(indicator.statusDetail().getStatus(), is(WARNING));
    }

    @Test
    public void shouldIndicateErrorStatus() {
        // given
        final StatusDetail initial = statusDetail("foo", WARNING, "message");
        final MutableStatusDetailIndicator indicator = new MutableStatusDetailIndicator(initial);
        // when
        indicator.toError("broken");
        // then
        assertThat(indicator.statusDetail().getName(), is("foo"));
        assertThat(indicator.statusDetail().getMessage(), is("broken"));
        assertThat(indicator.statusDetail().getStatus(), is(ERROR));
    }

    @Test
    public void shouldIndicateAdditionalDetails() {
        // given
        final StatusDetail initial = statusDetail("foo", WARNING, "message");
        final MutableStatusDetailIndicator indicator = new MutableStatusDetailIndicator(initial);
        // when
        indicator.withDetail("foo", "bar");
        // then
        assertThat(indicator.statusDetail().getName(), is("foo"));
        assertThat(indicator.statusDetail().getMessage(), is("message"));
        assertThat(indicator.statusDetail().getStatus(), is(WARNING));
        assertThat(indicator.statusDetail().getDetails(), hasEntry("foo", "bar"));
    }

    @Test
    public void shouldDeleteAdditionalDetail() {
        // given
        final StatusDetail initial = statusDetail("foo", WARNING, "message", singletonMap("foo", "baz"));
        final MutableStatusDetailIndicator indicator = new MutableStatusDetailIndicator(initial);
        // when
        indicator.withoutDetail("bar");
        // then
        assertThat(indicator.statusDetail().getName(), is("foo"));
        assertThat(indicator.statusDetail().getMessage(), is("message"));
        assertThat(indicator.statusDetail().getStatus(), is(WARNING));
        assertThat(indicator.statusDetail().getDetails(), not(hasEntry("bar", "baz")));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldFailToUpdateStatusWithDifferentName() {
        // given
        final MutableStatusDetailIndicator indicator = new MutableStatusDetailIndicator(statusDetail("foo", OK, "message"));
        // when
        indicator.update(statusDetail("bar", OK, "message"));
        // then an exception is thrown
    }

}
