package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.eventbus.JobEvents;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static de.otto.edison.jobs.definition.DefaultJobDefinition.manuallyTriggerableJobDefinition;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.*;
import static de.otto.edison.jobs.service.JobRunner.PING_PERIOD;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static java.time.Duration.ofSeconds;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class JobRunnerTest {

    @Mock
    private ScheduledExecutorService executor;
    @Mock
    private ScheduledFuture<?> scheduledJob;
    @Mock
    private JobEventPublisher jobEventPublisher;
    private JobRunner jobRunner;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        jobRunner = newJobRunner(
                "42",
                "NAME",
                executor,
                jobEventPublisher
        );

        doReturn(scheduledJob)
                .when(executor).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void shouldExecuteJob() {
        // given
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(fixedDelayJobDefinition("TYPE", "", "", ofSeconds(2), 0, empty()));

        // when
        jobRunner.start(jobRunnable);

        // then
        verify(jobRunnable).execute(jobEventPublisher);
    }

    @Test
    public void shouldPublishErrorMessageWithStackTraceOnFail() throws URISyntaxException {
        // given
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(fixedDelayJobDefinition("TYPE", "", "", ofSeconds(2), 0, empty()));
        doThrow(new RuntimeException("some error")).when(jobRunnable).execute(jobEventPublisher);

        // when
        jobRunner.start(jobRunnable);

        // then
        verify(jobEventPublisher).error(startsWith("Fatal error in job NAME (42)\n" +
                "java.lang.RuntimeException: some error\n"));
    }

    @Test
    public void shouldRestartJobOnException() {
        // given
        JobRunnable jobRunnable = mock(JobRunnable.class);

        when(jobRunnable.getJobDefinition())
                .thenReturn(manuallyTriggerableJobDefinition("someJobType", "someJobname", "Me is testjob", 2, Optional.empty()));
        doThrow(new RuntimeException("some error"))
                .when(jobRunnable).execute(eq(jobEventPublisher));

        // when
        jobRunner.start(jobRunnable);

        // then
        verify(jobEventPublisher).stateChanged(START);
        verify(jobRunnable, times(3)).execute(jobEventPublisher);
        verify(jobEventPublisher, times(2)).stateChanged(RESTART);
        verify(jobEventPublisher).stateChanged(STOP);
    }

    @Test
    public void shouldSendKeepAliveEventWithinPingJob() {
        // when
        jobRunner.start(getMockedRunnable());

        //then
        ArgumentCaptor<Runnable> pingRunnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).scheduleAtFixedRate(pingRunnableArgumentCaptor.capture(), eq(PING_PERIOD), eq(PING_PERIOD), eq(SECONDS));

        pingRunnableArgumentCaptor.getValue().run();

        verify(jobEventPublisher).stateChanged(KEEP_ALIVE);
    }

    @Test
    public void shouldStopPingJobWhenJobIsFinished() {
        // when
        jobRunner.start(getMockedRunnable());

        //then
        verify(scheduledJob).cancel(false);
    }

    @Test
    public void shouldInitJobEventsOnJobStart() {
        jobRunner.start(new StubRunnable(()-> JobEvents.info("test")));

        verify(jobEventPublisher).info("test");
    }

    @Test(expected = IllegalStateException.class)
    public void shouldDestroyReferenceToJobEventPublisherWhenJobFinishes() {
        jobRunner.start(getMockedRunnable());

        JobEvents.info("I should produce an error");
    }

    private JobRunnable getMockedRunnable() {
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(jobDefinition.jobType()).thenReturn("TYPE");
        when(jobRunnable.getJobDefinition()).thenReturn(jobDefinition);
        return jobRunnable;
    }

    private static class StubRunnable implements JobRunnable {

        private final Runnable runnable;

        StubRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public JobDefinition getJobDefinition() {
            JobDefinition jobDefinition = mock(JobDefinition.class);
            when(jobDefinition.jobType()).thenReturn("TYPE");
            return jobDefinition;
        }

        @Override
        public void execute(JobEventPublisher jobEventPublisher) {
            runnable.run();
        }
    }
}
