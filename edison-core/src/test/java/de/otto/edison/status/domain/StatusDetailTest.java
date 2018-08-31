package de.otto.edison.status.domain;

import org.junit.Test;

import java.util.Map;

import static de.otto.edison.status.domain.Status.ERROR;
import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;
import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public class StatusDetailTest {

    @Test
    public void shouldHaveAdditionalAttributes() {
        // given
        final StatusDetail statusDetail = statusDetail("foo", ERROR, "message", singletonMap("foo", "bar"));
        // when
        final Map<String,String> theMap = statusDetail.getDetails();
        // then
        assertThat(theMap, hasEntry("foo", "bar"));
    }

    @Test
    public void shouldResultInWarning() {
        // given
        StatusDetail statusDetail = statusDetail("foo", OK, "message", singletonMap("foo", "bar"));
        // when
        statusDetail = statusDetail.toWarning("different message");
        // then
        assertThat(statusDetail.getName(), is("foo"));
        assertThat(statusDetail.getMessage(), is("different message"));
        assertThat(statusDetail.getStatus(), is(WARNING));
        assertThat(statusDetail.getDetails(), hasEntry("foo", "bar"));
    }

    @Test
    public void shouldResultInError() {
        // given
        StatusDetail statusDetail = statusDetail("foo", WARNING, "message", singletonMap("foo", "bar"));
        // when
        statusDetail = statusDetail.toError("different message");
        // then
        assertThat(statusDetail.getName(), is("foo"));
        assertThat(statusDetail.getMessage(), is("different message"));
        assertThat(statusDetail.getStatus(), is(ERROR));
        assertThat(statusDetail.getDetails(), hasEntry("foo", "bar"));
    }

    @Test
    public void shouldResultInOk() {
        // given
        StatusDetail statusDetail = statusDetail("foo", WARNING, "message", singletonMap("foo", "bar"));
        // when
        statusDetail = statusDetail.toOk("different message");
        // then
        assertThat(statusDetail.getName(), is("foo"));
        assertThat(statusDetail.getMessage(), is("different message"));
        assertThat(statusDetail.getStatus(), is(OK));
        assertThat(statusDetail.getDetails(), hasEntry("foo", "bar"));
    }

    @Test
    public void shouldAddDetail() {
        // given
        StatusDetail statusDetail = statusDetail("foo", WARNING, "message", singletonMap("foo", "bar"));
        // when
        statusDetail = statusDetail.withDetail("bar", "baz");
        // then
        assertThat(statusDetail.getName(), is("foo"));
        assertThat(statusDetail.getMessage(), is("message"));
        assertThat(statusDetail.getStatus(), is(WARNING));
        assertThat(statusDetail.getDetails(), hasEntry("foo", "bar"));
        assertThat(statusDetail.getDetails(), hasEntry("bar", "baz"));
    }

    @Test
    public void shouldRemoveDetail() {
        // given
        StatusDetail statusDetail = statusDetail("foo", WARNING, "message", singletonMap("foo", "bar"));
        // when
        statusDetail = statusDetail.withoutDetail("foo");
        // then
        assertThat(statusDetail.getName(), is("foo"));
        assertThat(statusDetail.getMessage(), is("message"));
        assertThat(statusDetail.getStatus(), is(WARNING));
        assertThat(statusDetail.getDetails(), not(hasEntry("foo", "bar")));
    }

    @Test
    public void shouldOverwriteDetail() {
        // given
        StatusDetail statusDetail = statusDetail("foo", WARNING, "message", singletonMap("foo", "bar"));
        // when
        statusDetail = statusDetail.withDetail("foo","baz");
        // then
        assertThat(statusDetail.getName(), is("foo"));
        assertThat(statusDetail.getMessage(), is("message"));
        assertThat(statusDetail.getStatus(), is(WARNING));
        assertThat(statusDetail.getDetails(), hasEntry("foo", "baz"));
    }


}
