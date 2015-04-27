package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.Status;
import org.mockito.InOrder;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@Test
public class GracefulShutdownStatusIndicatorTest {

    @Test
    public void shouldIndicateOkOnStartup() throws Exception {
        // given
        GracefulShutdownStatusIndicator gracefulShutdownStatusIndicator;

        // when
        gracefulShutdownStatusIndicator = new GracefulShutdownStatusIndicator();

        // then
        assertThat(gracefulShutdownStatusIndicator.statusDetail().getStatus(), is(Status.OK));
    }

    @Test
    public void shouldIndicateErrorWhileShutdown() throws Exception {
        // given
        GracefulShutdownStatusIndicator gracefulShutdownStatusIndicator = spy(new GracefulShutdownStatusIndicator());
        Runnable runnable = mock(Runnable.class);

        doAnswer(invocation -> {
            assertThat(gracefulShutdownStatusIndicator.statusDetail().getStatus(), is(Status.OK));
            return null;
        }).when(gracefulShutdownStatusIndicator).waitForIndicateError();

        doAnswer(invocation -> {
            assertThat(gracefulShutdownStatusIndicator.statusDetail().getStatus(), is(Status.ERROR));
            return null;
        }).when(gracefulShutdownStatusIndicator).waitForShutdown();

        // when
        gracefulShutdownStatusIndicator.stop(runnable);

        // then
        InOrder order = inOrder(gracefulShutdownStatusIndicator, runnable);

        // on first wait call status should still be OK
        order.verify(gracefulShutdownStatusIndicator).waitForIndicateError();
        // on second wait call status should be switched to ERROR
        order.verify(gracefulShutdownStatusIndicator).waitForShutdown();
        // after second wait call shutdown chain should be executed
        order.verify(runnable).run();
    }
}
