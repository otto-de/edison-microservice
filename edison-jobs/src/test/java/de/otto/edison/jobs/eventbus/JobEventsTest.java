package de.otto.edison.jobs.eventbus;


import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class JobEventsTest {

    private JobEventPublisher jobEventPublisherMock = mock(JobEventPublisher.class);

    @After
    public void tearDown() throws Exception {
        JobEvents.deregister();
    }

    @Test
    public void shouldReportErrorViaJobEventPublisher() {
        JobEvents.register(jobEventPublisherMock);

        JobEvents.error("some error");

        verify(jobEventPublisherMock).error("some error");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowErrorIfJobEventsAreAlreadyRegistered() {
        JobEvents.register(jobEventPublisherMock);
        JobEvents.register(jobEventPublisherMock);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowErrorIfNotSetupOnInfo() {
        JobEvents.info("Some info");
        verifyZeroInteractions(jobEventPublisherMock);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowErrorIfNotSetupOnWarning() {
        JobEvents.warn("Some warning");
        verifyZeroInteractions(jobEventPublisherMock);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowErrorIfNotSetupOnError() {
        JobEvents.error("Some error");
        verifyZeroInteractions(jobEventPublisherMock);
    }

    @Test
    public void shouldReportToDifferentPublishersInSecondThread() throws InterruptedException {
        JobEventPublisher firstPublisher = mock(JobEventPublisher.class);
        JobEventPublisher secondPublisher = mock(JobEventPublisher.class);

        //Guarantee use of different threads than the main thread.
        ExecutorService e = Executors.newFixedThreadPool(2);
        e.submit(infoRunnable(firstPublisher));
        e.submit(infoRunnable(secondPublisher));
        e.shutdown();
        e.awaitTermination(500, TimeUnit.MILLISECONDS);

        assertThat(e.isTerminated(), is(true));
        verify(firstPublisher, times(1)).info("Info");
        verify(secondPublisher, times(1)).info("Info");
        verifyZeroInteractions(jobEventPublisherMock);
    }

    private Runnable infoRunnable(JobEventPublisher jobEventPublisher) {
        return () -> {
            JobEvents.register(jobEventPublisher);
            JobEvents.info("Info");
            JobEvents.deregister();
        };
    }
}