package de.otto.edison.jobs.service;

import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.eventbus.events.MessageEvent;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static de.otto.edison.jobs.definition.DefaultJobDefinition.manuallyTriggerableJobDefinition;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.RESTART;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.START;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.STILL_ALIVE;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.STOP;
import static de.otto.edison.jobs.service.JobRunner.PING_PERIOD;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static java.net.URI.create;
import static java.time.Clock.fixed;
import static java.time.Duration.ofSeconds;
import static java.time.ZoneId.systemDefault;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class JobRunnerTest {

    private Clock clock;
    private ScheduledExecutorService executor;
    private ScheduledFuture scheduledJob;
    private JobEventPublisher jobEventPublisher;

    @BeforeMethod
    public void setUp() throws Exception {
        clock = fixed(Instant.now(), systemDefault());
        executor = mock(ScheduledExecutorService.class);
        jobEventPublisher = mock(JobEventPublisher.class);

        scheduledJob = mock(ScheduledFuture.class);
        doReturn(scheduledJob)
                .when(executor).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void shouldExecuteJob() {
        // given
        JobRunner jobRunner = newJobRunner(create("/foo/jobs/42"), "TYPE", executor, jobEventPublisher);
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(fixedDelayJobDefinition("TYPE", "", "", ofSeconds(2), 0, empty()));

        // when
        jobRunner.start(jobRunnable);

        // then
        verify(jobRunnable).execute(jobEventPublisher);
    }

    @Test
    public void shouldPublishErrorMessageOnFail() throws URISyntaxException {
        // given
        URI jobUri = create("/foo/jobs/42");
        JobRunner jobRunner = newJobRunner(
                jobUri,
                "NAME",
                executor,
                jobEventPublisher);
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(fixedDelayJobDefinition("TYPE", "", "", ofSeconds(2), 0, empty()));
        doThrow(new RuntimeException("some error")).when(jobRunnable).execute(jobEventPublisher);

        // when
        jobRunner.start(jobRunnable);

        // then
        verify(jobEventPublisher).message(eq(MessageEvent.Level.ERROR), contains("some error"));
    }

    @Test
    public void shouldRestartJobOnException() {
        // given
        URI jobUri = create("/foo/jobs/42");
        JobRunner jobRunner = newJobRunner(
                jobUri,
                "NAME",
                executor,
                jobEventPublisher);

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
        //given
        URI jobUri = create("/foo/jobs/42");
        JobRunner jobRunner = newJobRunner(
                jobUri,
                "NAME",
                executor,
                jobEventPublisher);

        // when
        jobRunner.start(mock(JobRunnable.class));

        //then
        ArgumentCaptor<Runnable> pingRunnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).scheduleAtFixedRate(pingRunnableArgumentCaptor.capture(), eq(PING_PERIOD), eq(PING_PERIOD), eq(SECONDS));

        pingRunnableArgumentCaptor.getValue().run();

        verify(jobEventPublisher).stateChanged(STILL_ALIVE);
    }

    @Test
    public void shouldStopPingJobWhenJobIsFinished() {
        //given
        JobRunner jobRunner = newJobRunner(
                create("/foo/jobs/42"),
                "NAME",
                executor,
                jobEventPublisher);

        // when
        jobRunner.start(mock(JobRunnable.class));

        //then
        verify(scheduledJob).cancel(false);
    }
}
