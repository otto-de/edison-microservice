package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.domain.Level;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class JobEventsShutdownListenerTest {

    @Test
    public void shouldBroadcastMessageToJobEventsWhenShuttingDown() throws Exception {
        try {
            // given
            JobEventsShutdownListener jobEventsShutdownListener = spy(new JobEventsShutdownListener());
            Runnable runnable = mock(Runnable.class);

            JobEventPublisher jobEventPublisherMock = mock(JobEventPublisher.class);
            JobEvents.register(jobEventPublisherMock);

            //when
            jobEventsShutdownListener.stop(runnable);

            //then
            verify(jobEventPublisherMock).message(Level.ERROR, "Service is shutting down, this job will (likely) be cancelled.");
            verify(runnable).run();
        } finally {
            JobEvents.deregister();
        }
    }
}