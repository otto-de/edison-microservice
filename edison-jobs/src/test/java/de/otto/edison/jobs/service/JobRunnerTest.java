package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static de.otto.edison.jobs.definition.DefaultJobDefinition.manuallyTriggerableJobDefinition;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.*;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.newStateChangeEvent;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static java.time.Duration.ofSeconds;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class JobRunnerTest {
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ScheduledExecutorService executor;
    @Mock
    private ScheduledFuture<?> scheduledJob;

    @BeforeEach
    public void setUp() throws Exception {
        openMocks(this);

        doReturn(scheduledJob)
                .when(executor)
                .scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void shouldExecuteJob() {
        // given
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(
                fixedDelayJobDefinition("TYPE", "", "", ofSeconds(2), 0, empty())
        );
        JobRunner jobRunner = jobRunner(jobRunnable);

        // when
        jobRunner.run();

        // then
        verify(jobRunnable).execute(any(JobEventPublisher.class));
    }

    @Test
    public void shouldSetMDC() {
        // given
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(
                fixedDelayJobDefinition("TYPE", "", "", ofSeconds(2), 0, empty())
        );
        JobRunner jobRunner = jobRunner(jobRunnable);

        // when
        jobRunner.start();

        // then
        assertThat(MDC.get("job_id"), is("42"));
        assertThat(MDC.get("job_type"), is("TYPE"));
    }

    @Test
    public void shouldSendLifecycleEvents() {
        // given
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(
                fixedDelayJobDefinition("TYPE", "", "", ofSeconds(2), 0, empty())
        );
        JobRunner jobRunner = jobRunner(jobRunnable);

        // when
        jobRunner.run();

        // then
        verify(eventPublisher).publishEvent(newStateChangeEvent(jobRunnable, "42", START));
        verify(eventPublisher).publishEvent(newStateChangeEvent(jobRunnable, "42", STOP));
    }

    @Test
    public void shouldMarkJobSkipped() {
        // given
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(
                fixedDelayJobDefinition("TYPE", "", "", ofSeconds(2), 0, empty())
        );
        when(jobRunnable.execute()).thenReturn(false);
        JobRunner jobRunner = jobRunner(jobRunnable);

        // when
        jobRunner.run();

        // then
        verify(eventPublisher).publishEvent(newStateChangeEvent(jobRunnable, "42", SKIPPED));
    }

    @Test
    public void shouldRestartJobOnException() {
        // given
        JobRunnable jobRunnable = mock(JobRunnable.class);

        when(jobRunnable.getJobDefinition())
                .thenReturn(manuallyTriggerableJobDefinition("someJobType", "someJobname", "Me is testjob", 2, Optional.empty()));
        doThrow(new RuntimeException("some error"))
                .when(jobRunnable).execute(any(JobEventPublisher.class));
        JobRunner jobRunner = jobRunner(jobRunnable);

        // when
        jobRunner.run();

        // then
        verify(eventPublisher)
                .publishEvent(newStateChangeEvent(jobRunnable, "42", START));
        verify(jobRunnable, times(3))
                .execute(any(JobEventPublisher.class));
        verify(eventPublisher, times(2))
                .publishEvent(newStateChangeEvent(jobRunnable, "42", RESTART));
        verify(eventPublisher)
                .publishEvent(newStateChangeEvent(jobRunnable, "42", STOP));
    }

    @Test
    public void shouldSendKeepAliveEventWithinPingJob() {
        // given
        final JobRunnable jobRunnable = getMockedRunnable();
        JobRunner jobRunner = jobRunner(jobRunnable);

        // when
        jobRunner.run();

        //then
        ArgumentCaptor<Runnable> pingRunnableArgumentCaptor = forClass(Runnable.class);
        verify(executor).scheduleAtFixedRate(
                pingRunnableArgumentCaptor.capture(),
                eq(20L),
                eq(20L),
                eq(SECONDS)
        );

        pingRunnableArgumentCaptor.getValue().run();

        verify(eventPublisher).publishEvent(
                newStateChangeEvent(jobRunnable, "42", KEEP_ALIVE)
        );
    }

    @Test
    public void shouldStopPingJobWhenJobIsFinished() {
        // given
        final JobRunnable jobRunnable = getMockedRunnable();
        JobRunner jobRunner = jobRunner(jobRunnable);

        // when
        jobRunner.run();

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

    private JobRunner jobRunner(final JobRunnable jobRunnable) {
        return newJobRunner(
                "42",
                jobRunnable,
                eventPublisher,
                executor
        );
    }
}
