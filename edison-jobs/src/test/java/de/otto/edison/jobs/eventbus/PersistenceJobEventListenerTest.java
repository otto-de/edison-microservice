package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import de.otto.edison.jobs.service.JobRunnable;
import de.otto.edison.jobs.service.JobService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static de.otto.edison.jobs.domain.Level.INFO;
import static de.otto.edison.jobs.eventbus.events.MessageEvent.newMessageEvent;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.*;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.newStateChangeEvent;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PersistenceJobEventListenerTest {

    public static final String JOB_ID = "some/job/id";
    public static final String JOB_TYPE = "jobType";

    @Mock
    private JobService jobServiceMock;
    @Mock
    private JobRunnable jobRunnableMock;

    private PersistenceJobEventListener subject;



    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(jobRunnableMock.getJobDefinition()).thenReturn(mock(JobDefinition.class));

        subject = new PersistenceJobEventListener(jobServiceMock);
    }

    @Test
    public void shouldPersistStillAliveEvent() throws Exception {
        subject.consumeStateChange(stateChangedEvent(KEEP_ALIVE));

        verify(jobServiceMock).keepAlive(JOB_ID);
    }

    @Test
    public void shouldPersistRestartEvent() throws Exception {
        subject.consumeStateChange(stateChangedEvent(RESTART));

        verify(jobServiceMock).markRestarted(JOB_ID);
    }

    @Test
    public void shouldPersistDeadEvent() throws Exception {
        JobDefinition mockDefinition = mock(JobDefinition.class);
        when(mockDefinition.jobType()).thenReturn(JOB_TYPE);
        when(jobRunnableMock.getJobDefinition()).thenReturn(mockDefinition);
        subject.consumeStateChange(stateChangedEvent(DEAD));

        verify(jobServiceMock).killJob(JOB_ID, JOB_TYPE);
    }

    @Test
    public void shouldPersistStopEvent() throws Exception {
        subject.consumeStateChange(stateChangedEvent(STOP));

        verify(jobServiceMock).stopJob(JOB_ID);
    }

    @Test
    public void shouldPersistSkippedEvent() throws Exception {
        subject.consumeStateChange(stateChangedEvent(SKIPPED));

        verify(jobServiceMock).markSkipped(JOB_ID);
    }

    @Test
    public void shouldPersistMessage() throws Exception {
        MessageEvent messageEvent = newMessageEvent(jobRunnableMock, JOB_ID, INFO, "some message", Optional.empty());
        OffsetDateTime timestamp = OffsetDateTime.ofInstant(Instant.ofEpochMilli(messageEvent.getTimestamp()), ZoneId.systemDefault());

        subject.consumeMessage(messageEvent);

        verify(jobServiceMock).appendMessage(JOB_ID, JobMessage.jobMessage(INFO, "some message", timestamp));
    }

    @Test
    public void shouldNotThrowIfSomethingFailsInDatabase() {
        MessageEvent messageEvent = newMessageEvent(jobRunnableMock, JOB_ID, INFO, "some message", Optional.empty());
        OffsetDateTime timestamp = OffsetDateTime.ofInstant(Instant.ofEpochMilli(messageEvent.getTimestamp()), ZoneId.systemDefault());
        final JobMessage expectedJobMessage = JobMessage.jobMessage(INFO, "some message", timestamp);
        doThrow(new RuntimeException("Miserable failure")).when(jobServiceMock).appendMessage(JOB_ID, expectedJobMessage);

        subject.consumeMessage(messageEvent);

        verify(jobServiceMock).appendMessage(JOB_ID, expectedJobMessage);
    }


    @Test
    public void shouldNotThrowIfStateChangeFailsInDatabase() throws Exception {
        doThrow(new RuntimeException("Unexpected disturbance in the force")).when(jobServiceMock).stopJob(JOB_ID);
        subject.consumeStateChange(stateChangedEvent(STOP));

        verify(jobServiceMock).stopJob(JOB_ID);
    }

    private StateChangeEvent stateChangedEvent(StateChangeEvent.State stop) {
        return newStateChangeEvent(jobRunnableMock, JOB_ID, stop);
    }

}
