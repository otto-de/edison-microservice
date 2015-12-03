package de.otto.edison.testsupport.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;

import static java.time.ZoneId.systemDefault;

public final class TestClock extends Clock {

    private Instant current;

    public static TestClock now() {
        return new TestClock(Instant.now());
    }

    public static TestClock now(final Clock clock) {
        return new TestClock(Instant.now(clock));
    }

    public static TestClock now(final long millis) {
        return new TestClock(Instant.ofEpochMilli(millis));
    }

    private TestClock(final Instant current) {
        this.current = current;
    }

    @Override
    public ZoneId getZone() {
        return systemDefault();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Instant instant() {
        return current;
    }

    public void proceed(final long amount, final TemporalUnit unit) {
        current = current.plus(amount, unit);
    }
}
