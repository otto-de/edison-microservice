package de.otto.edison.togglz.activation;

import org.junit.jupiter.api.Test;
import org.togglz.core.repository.FeatureState;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeRangeActivationStrategyTest {
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    public void shouldBeActiveWhenNowIsBetweenFromAndTo() {
        // given
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        final String from = format.format(now.minusHours(1));
        final String to = format.format(now.plusHours(1));

        final FeatureState state = setupToggle(from, to);

        final TimeRangeActivationStrategy strategy = new TimeRangeActivationStrategy();

        // when
        final boolean active = strategy.isActive(state, null);

        // then
        assertTrue(active);
    }

    @Test
    public void shouldNotBeActiveIfToIsBeforeNow() {
        // given
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        final String from = format.format(now.minusHours(2));
        final String to = format.format(now.minusHours(1));

        final FeatureState state = setupToggle(from, to);
        final TimeRangeActivationStrategy strategy = new TimeRangeActivationStrategy();

        // when
        final boolean active = strategy.isActive(state, null);

        // then
        assertFalse(active);
    }

    @Test
    public void shouldNotBeActiveIfFromIsAfterNow() {
        // given
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        final String from = format.format(now.plusHours(1));
        final String to = format.format(now.plusHours(2));

        final FeatureState state = setupToggle(from, to);
        final TimeRangeActivationStrategy strategy = new TimeRangeActivationStrategy();

        // when
        final boolean active = strategy.isActive(state, null);

        // then
        assertFalse(active);
    }

    @Test
    public void shouldNotBeActiveIfToIsBeforeFrom() {
        // given
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        final String from = format.format(now.plusHours(1));
        final String to = format.format(now.minusHours(1));

        final FeatureState state = setupToggle(from, to);
        final TimeRangeActivationStrategy strategy = new TimeRangeActivationStrategy();

        // when
        final boolean active = strategy.isActive(state, null);

        // then
        assertFalse(active);
    }

    @Test
    public void shouldNotBeActiveOnInvalidExpression() {
        // given
        final FeatureState state = setupToggle("someInvalidDate", LocalDateTime.now(ZoneId.of("UTC")).toString());
        final TimeRangeActivationStrategy strategy = new TimeRangeActivationStrategy();

        // when
        final boolean active = strategy.isActive(state, null);

        // then
        assertFalse(active);
    }

    @Test
    public void shouldBeInactiveWhenTimeZonesDoNotMatch() {
        // given
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Paris"));
        final String from = format.format(now.minusHours(1));
        final String to = format.format(now.plusHours(1));

        final FeatureState state = setupToggle(from, to);

        final TimeRangeActivationStrategy strategy = new TimeRangeActivationStrategy();

        // when
        final boolean active = strategy.isActive(state, null);

        // then
        assertFalse(active);
    }

    private FeatureState setupToggle(final String from, final String to) {
        final FeatureState toggleState = new FeatureState(null);
        toggleState.setEnabled(true);
        toggleState.setParameter("from", from);
        toggleState.setParameter("to", to);
        return toggleState;
    }

}