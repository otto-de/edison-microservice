package de.otto.edison.status.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
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
import static org.hamcrest.Matchers.nullValue;

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

    @Test
    public void shouldKeepSinceWhenStatusIsUnchanged() {
        // given
        final Instant since = Instant.parse("2024-01-01T10:00:00Z");
        final StatusDetail statusDetail = statusDetail("foo", WARNING, "message").withSince(since);
        // then
        assertThat(statusDetail.withDetail("foo", "bar").getSince(), is(since));
        assertThat(statusDetail.withoutDetail("foo").getSince(), is(since));
        assertThat(statusDetail.toWarning("different message").getSince(), is(since));
    }

    @Test
    public void shouldDropSinceWhenStatusChanges() {
        // given
        final StatusDetail statusDetail = statusDetail("foo", WARNING, "message")
                .withSince(Instant.parse("2024-01-01T10:00:00Z"));
        // then
        assertThat(statusDetail.toOk("different message").getSince(), is(nullValue()));
        assertThat(statusDetail.toError("different message").getSince(), is(nullValue()));
    }

    @Test
    public void shouldIncludeSinceInEqualsAndHashCode() {
        // given
        final StatusDetail statusDetail = statusDetail("foo", WARNING, "message");
        final StatusDetail withSince = statusDetail.withSince(Instant.parse("2024-01-01T10:00:00Z"));
        final StatusDetail withSameSince = statusDetail.withSince(Instant.parse("2024-01-01T10:00:00Z"));
        final StatusDetail withOtherSince = statusDetail.withSince(Instant.parse("2024-01-01T11:00:00Z"));
        // then
        assertThat(withSince, is(not(statusDetail)));
        assertThat(withSince, is(withSameSince));
        assertThat(withSince.hashCode(), is(withSameSince.hashCode()));
        assertThat(withSince, is(not(withOtherSince)));
    }

}
