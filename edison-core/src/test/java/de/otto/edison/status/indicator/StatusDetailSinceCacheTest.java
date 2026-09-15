package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.StatusDetail;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static de.otto.edison.status.domain.Status.ERROR;
import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;
import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class StatusDetailSinceCacheTest {

    private static final Instant T0 = Instant.parse("2024-01-01T10:00:00Z");
    private static final Instant T1 = Instant.parse("2024-01-01T10:00:10Z");
    private static final Instant T2 = Instant.parse("2024-01-01T10:00:20Z");

    private final MutableClock clock = new MutableClock(T0);
    private final StatusDetailSinceCache cache = new StatusDetailSinceCache(clock);

    @Test
    public void shouldSetSinceOnFirstOccurrence() {
        // when
        final List<StatusDetail> result = cache.withSince(singletonList(statusDetail("foo", OK, "a message")));
        // then
        assertThat(result.get(0).getSince(), is(T0));
    }

    @Test
    public void shouldKeepSinceWhenStatusDoesNotChange() {
        // given
        cache.withSince(singletonList(statusDetail("foo", OK, "a message")));
        // when
        clock.setTo(T1);
        final List<StatusDetail> result = cache.withSince(singletonList(statusDetail("foo", OK, "a message")));
        // then
        assertThat(result.get(0).getSince(), is(T0));
    }

    @Test
    public void shouldKeepSinceWhenOnlyMessageChanges() {
        // given
        cache.withSince(singletonList(statusDetail("foo", OK, "a message")));
        // when
        clock.setTo(T1);
        final List<StatusDetail> result = cache.withSince(singletonList(statusDetail("foo", OK, "a different message")));
        // then
        assertThat(result.get(0).getSince(), is(T0));
    }

    @Test
    public void shouldKeepSinceWhenOnlyDetailsChange() {
        // given
        cache.withSince(singletonList(statusDetail("foo", OK, "a message")));
        // when
        clock.setTo(T1);
        final List<StatusDetail> result = cache.withSince(
                singletonList(statusDetail("foo", OK, "a message", singletonMap("key", "value"))));
        // then
        assertThat(result.get(0).getSince(), is(T0));
    }

    @Test
    public void shouldUpdateSinceWhenStatusChanges() {
        // given
        cache.withSince(singletonList(statusDetail("foo", OK, "a message")));
        // when
        clock.setTo(T1);
        final List<StatusDetail> result = cache.withSince(singletonList(statusDetail("foo", WARNING, "a message")));
        // then
        assertThat(result.get(0).getSince(), is(T1));
    }

    @Test
    public void shouldKeepSinceOfNewStatusAfterItChanged() {
        // given
        cache.withSince(singletonList(statusDetail("foo", OK, "a message")));
        clock.setTo(T1);
        cache.withSince(singletonList(statusDetail("foo", ERROR, "a message")));
        // when
        clock.setTo(T2);
        final List<StatusDetail> result = cache.withSince(singletonList(statusDetail("foo", ERROR, "a message")));
        // then
        assertThat(result.get(0).getSince(), is(T1));
    }

    @Test
    public void shouldTrackStatusDetailsIndependentlyByName() {
        // given
        cache.withSince(List.of(statusDetail("foo", OK, "a message"), statusDetail("bar", OK, "a message")));
        // when
        clock.setTo(T1);
        final List<StatusDetail> result = cache.withSince(
                List.of(statusDetail("foo", ERROR, "a message"), statusDetail("bar", OK, "a message")));
        // then
        assertThat(result.get(0).getSince(), is(T1));
        assertThat(result.get(1).getSince(), is(T0));
    }

    @Test
    public void shouldForgetStatusDetailsThatDisappeared() {
        // given
        cache.withSince(List.of(statusDetail("foo", OK, "a message"), statusDetail("bar", OK, "a message")));
        // when "bar" disappears and comes back later
        clock.setTo(T1);
        cache.withSince(singletonList(statusDetail("foo", OK, "a message")));
        clock.setTo(T2);
        final List<StatusDetail> result = cache.withSince(
                List.of(statusDetail("foo", OK, "a message"), statusDetail("bar", OK, "a message")));
        // then
        assertThat(result.get(0).getSince(), is(T0));
        assertThat(result.get(1).getSince(), is(T2));
    }

    @Test
    public void shouldResetSinceAfterClear() {
        // given
        cache.withSince(singletonList(statusDetail("foo", OK, "a message")));
        // when
        cache.clear();
        clock.setTo(T1);
        final List<StatusDetail> result = cache.withSince(singletonList(statusDetail("foo", OK, "a message")));
        // then
        assertThat(result.get(0).getSince(), is(T1));
    }

    @Test
    public void shouldNotModifyTheOtherProperties() {
        // when
        final StatusDetail result = cache.withSince(
                singletonList(statusDetail("foo", WARNING, "a message", singletonMap("key", "value")))).get(0);
        // then
        assertThat(result.getName(), is("foo"));
        assertThat(result.getStatus(), is(WARNING));
        assertThat(result.getMessage(), is("a message"));
        assertThat(result.getDetails(), is(singletonMap("key", "value")));
    }

    @Test
    public void shouldUseSystemClockByDefault() {
        // when
        final StatusDetail result = new StatusDetailSinceCache()
                .withSince(singletonList(statusDetail("foo", OK, "a message"))).get(0);
        // then
        assertThat(result.getSince(), is(notNullValue()));
    }

    @Test
    public void shouldNotSetSinceOnPlainStatusDetails() {
        assertThat(statusDetail("foo", OK, "a message").getSince(), is(nullValue()));
    }

    private static class MutableClock extends Clock {
        private volatile Instant instant;

        private MutableClock(final Instant instant) {
            this.instant = instant;
        }

        private void setTo(final Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(final java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
