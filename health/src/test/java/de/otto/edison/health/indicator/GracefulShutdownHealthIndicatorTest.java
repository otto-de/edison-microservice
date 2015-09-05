package de.otto.edison.health.indicator;

import org.mockito.InOrder;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.up;

@Test
public class GracefulShutdownHealthIndicatorTest {

    private static final int SOME_LONG_WHICH_WE_DONT_CARE_ABOUT = 7;

    @Test
    public void shouldHealthyOnStartup() throws Exception {
        // given
        GracefulShutdownHealthIndicator gracefulShutdownHealthIndicator;

        // when
        gracefulShutdownHealthIndicator = new GracefulShutdownHealthIndicator(SOME_LONG_WHICH_WE_DONT_CARE_ABOUT, SOME_LONG_WHICH_WE_DONT_CARE_ABOUT);

        // then
        assertThat(gracefulShutdownHealthIndicator.health(), is(up().build()));
    }

    @Test
    public void shouldIndicateErrorWhileShutdown() throws Exception {
        // given
        GracefulShutdownHealthIndicator gracefulShutdownHealthIndicator = spy(
                new GracefulShutdownHealthIndicator(SOME_LONG_WHICH_WE_DONT_CARE_ABOUT, SOME_LONG_WHICH_WE_DONT_CARE_ABOUT));
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
