package de.otto.edison.health.indicator;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.up;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;

public class ApplicationHealthIndicatorTest {

    @Test
    public void shouldIndicateHealth() {
        // given
        ApplicationHealthIndicator indicator = new ApplicationHealthIndicator();
        // when
        indicator.indicateHealth(down().build());
        // then
        assertThat(indicator.health().getStatus(), is(DOWN));
    }

    @Test
    public void shouldIndicateHealthOkAfterError() {
        // given
        ApplicationHealthIndicator indicator = new ApplicationHealthIndicator();
        indicator.indicateHealth(down().build());
        // when
        indicator.indicateHealth(up().build());
        // then
        assertThat(indicator.health().getStatus(), is(UP));
    }
}
