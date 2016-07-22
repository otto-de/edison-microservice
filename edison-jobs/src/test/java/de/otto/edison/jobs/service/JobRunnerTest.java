package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class JobRunnerTest {

    private ScheduledExecutorService executor;
    private ScheduledFuture scheduledJob;
    private JobEventPublisher jobEventPublisher;

    @BeforeMethod
    public void setUp() throws Exception {
        executor = mock(ScheduledExecutorService.class);
        jobEventPublisher = mock(JobEventPublisher.class);

        scheduledJob = mock(ScheduledFuture.class);
        doReturn(scheduledJob)
                .when(executor).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void shouldExecuteJob() {
        // given
        JobRunner jobRunner = newJobRunner("42", "TYPE", executor, jobEventPublisher);
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
        JobRunner jobRunner = newJobRunner(
                "42",
                "NAME",
                executor,
                jobEventPublisher
        );
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
        JobRunner jobRunner = newJobRunner(
                "42",
                "NAME",
                executor,
                jobEventPublisher
        );

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
        JobRunner jobRunner = newJobRunner(
                "42",
                "NAME",
                executor,
                jobEventPublisher
        );

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
        //given
        JobRunner jobRunner = newJobRunner(
                "42",
                "NAME",
                executor,
                jobEventPublisher
        );

        // when
        jobRunner.start(getMockedRunnable());

        //then
        verify(scheduledJob).cancel(false);
    }

    private JobRunnable getMockedRunnable() {
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(jobDefinition.jobType()).thenReturn("TYPE");
        when(jobRunnable.getJobDefinition()).thenReturn(jobDefinition);
        return jobRunnable;
    }
}
