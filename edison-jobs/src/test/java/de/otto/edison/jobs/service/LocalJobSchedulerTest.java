package de.otto.edison.jobs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class LocalJobSchedulerTest {

    @Mock
    private JobRunnable fixedDelayJobRunnable;
    @Mock
    private JobRunnable manualJobRunnable;
    @Mock
    private JobRunnable cronJobRunnable;

    @Mock
    private JobService jobService;
    @Mock
    private TaskScheduler taskScheduler;

    @BeforeEach
    public void setUp() {
        openMocks(this);
        when(fixedDelayJobRunnable.getJobDefinition()).thenReturn(
                fixedDelayJobDefinition("FIXED", "", "", Duration.ofSeconds(2), 0, Optional.empty())
        );
        when(manualJobRunnable.getJobDefinition()).thenReturn(
                manuallyTriggerableJobDefinition("MANUAL", "", "", 0, Optional.empty())
        );
        when(cronJobRunnable.getJobDefinition()).thenReturn(
                cronJobDefinition("CRON", "", "", "0 0 * * * *", 0, Optional.empty())
        );
    }

    @Test
    public void shouldScheduleRunnable() {
        // given
        LocalJobScheduler localJobScheduler = new LocalJobScheduler(List.of(fixedDelayJobRunnable, cronJobRunnable), jobService, taskScheduler);

        // when
        localJobScheduler.schedule();

        // then
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(taskScheduler, times(2)).schedule(any(), triggerCaptor.capture());

        assertTrue(triggerCaptor.getAllValues().stream().anyMatch(trigger -> trigger instanceof CronTrigger));
        assertTrue(triggerCaptor.getAllValues().stream().anyMatch(trigger -> trigger instanceof PeriodicTrigger));
    }

    @Test
    public void shouldStartJobFromRunnable() {
        // given
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskScheduler).schedule(any(), (Trigger) any());
        LocalJobScheduler localJobScheduler = new LocalJobScheduler(List.of(fixedDelayJobRunnable, cronJobRunnable), jobService, taskScheduler);

        // when
        localJobScheduler.schedule();

        // then
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(jobService, times(2)).startAsyncJob(stringCaptor.capture());
        assertTrue(stringCaptor.getAllValues().contains("FIXED"));
        assertTrue(stringCaptor.getAllValues().contains("CRON"));
    }

    @Test
    public void shouldFilterManuallyTriggeredJobs() {
        // given
        LocalJobScheduler localJobScheduler = new LocalJobScheduler(List.of(fixedDelayJobRunnable, cronJobRunnable, manualJobRunnable), jobService, taskScheduler);

        // when
        localJobScheduler.schedule();

        // then
        verify(taskScheduler, times(2)).schedule(any(), (Trigger) any());
    }
}