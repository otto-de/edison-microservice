package de.otto.edison.jobs.eventbus;


import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class JobEventsTest {

    private JobEventPublisher jobEventPublisherMock = mock(JobEventPublisher.class);

    @AfterMethod
    public void tearDown() throws Exception {
        JobEvents.destroy();
    }

    @Test
    public void shouldReportErrorViaJobEventPublisher() {
        JobEvents.init(jobEventPublisherMock);

        JobEvents.error("some error");

        verify(jobEventPublisherMock).error("some error");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowErrorIfJobEventsAreAlreadyInitialised() {
        JobEvents.init(jobEventPublisherMock);
        JobEvents.init(jobEventPublisherMock);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowErrorIfNotSetupOnInfo() {
        JobEvents.info("Some info");
        verifyZeroInteractions(jobEventPublisherMock);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowErrorIfNotSetupOnWarning() {
        JobEvents.warn("Some warning");
        verifyZeroInteractions(jobEventPublisherMock);
    }

    @Test(expectedExceptions = IllegalStateException.class)
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
            JobEvents.init(jobEventPublisher);
            JobEvents.info("Info");
            JobEvents.destroy();
        };
    }
}