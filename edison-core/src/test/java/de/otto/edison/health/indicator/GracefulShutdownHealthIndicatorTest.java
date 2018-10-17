package de.otto.edison.health.indicator;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.up;

public class GracefulShutdownHealthIndicatorTest {

    @Test
    public void shouldHealthyOnStartup() throws Exception {
        // given
        GracefulShutdownHealthIndicator gracefulShutdownHealthIndicator;

        // when
        gracefulShutdownHealthIndicator = new GracefulShutdownHealthIndicator(mock(GracefulShutdownProperties.class));

        // then
        assertThat(gracefulShutdownHealthIndicator.health(), is(up().build()));
    }

    @Test
    public void shouldIndicateErrorWhileShutdown() throws Exception {
        // given
        class TestGracefulShutdownHealthIndicator extends GracefulShutdownHealthIndicator {
            boolean waitForShutdownCalled = false;

            TestGracefulShutdownHealthIndicator(GracefulShutdownProperties properties) {
                super(properties);
            }

            @Override
            void waitForShutdown() throws InterruptedException {
                assertThat(health(), is(down().build()));
                waitForShutdownCalled = true;
                super.waitForShutdown();
            }
        }
        TestGracefulShutdownHealthIndicator gracefulShutdownHealthIndicator =
                new TestGracefulShutdownHealthIndicator(mock(GracefulShutdownProperties.class));
        Runnable runnable = mock(Runnable.class);

        // when
        gracefulShutdownHealthIndicator.stop(runnable);

        // then
        assertThat(gracefulShutdownHealthIndicator.waitForShutdownCalled, is(true));
    }
}
