package de.otto.µservice.status.indicator;

import de.otto.µservice.status.domain.StatusDetail;
import org.testng.annotations.Test;

import static de.otto.µservice.status.domain.Status.ERROR;
import static de.otto.µservice.status.domain.Status.OK;
import static de.otto.µservice.status.domain.StatusDetail.statusDetail;
import static org.hamcrest.MatcherAssert.assertThat;
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

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldFailToUpdateStatusWithDifferentName() {
        // given
        final MutableStatusDetailIndicator indicator = new MutableStatusDetailIndicator(statusDetail("foo", OK, "message"));
        // when
        indicator.update(statusDetail("bar", OK, "message"));
        // then an exception is thrown
    }

}
