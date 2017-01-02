package de.otto.edison.health.indicator;

import org.junit.Test;
import org.mockito.InOrder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
        GracefulShutdownHealthIndicator gracefulShutdownHealthIndicator = spy(
                new GracefulShutdownHealthIndicator(mock(GracefulShutdownProperties.class)));
        Runnable runnable = mock(Runnable.class);

        doAnswer(invocation -> {
            assertThat(gracefulShutdownHealthIndicator.health(), is(up().build()));
            return null;
        }).when(gracefulShutdownHealthIndicator).waitForSettingHealthCheckToDown();

        doAnswer(invocation -> {
            assertThat(gracefulShutdownHealthIndicator.health(), is(down().build()));
            return null;
        }).when(gracefulShutdownHealthIndicator).waitForShutdown();

        // when
        gracefulShutdownHealthIndicator.stop(runnable);

        // then
        InOrder order = inOrder(gracefulShutdownHealthIndicator, runnable);

        // on first wait call status should still be OK
        order.verify(gracefulShutdownHealthIndicator).waitForSettingHealthCheckToDown();
        // on second wait call status should be switched to ERROR
        order.verify(gracefulShutdownHealthIndicator).waitForShutdown();
        // after second wait call shutdown chain should be executed
        order.verify(runnable).run();
    }
}
