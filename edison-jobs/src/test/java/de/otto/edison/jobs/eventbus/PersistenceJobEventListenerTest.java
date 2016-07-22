package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import de.otto.edison.jobs.service.JobRunnable;
import de.otto.edison.jobs.service.JobService;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static de.otto.edison.jobs.eventbus.events.MessageEvent.newMessageEvent;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.*;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.newStateChangeEvent;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@Test
public class PersistenceJobEventListenerTest {

    public static final String JOB_ID = "some/job/id";

    @Mock
    private JobService jobServiceMock;
    @Mock
    private JobRunnable jobRunnableMock;

    private PersistenceJobEventListener subject;



    @BeforeMethod
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
        subject.consumeStateChange(stateChangedEvent(DEAD));

        verify(jobServiceMock).killJob(JOB_ID);
    }

    @Test
    public void shouldPersistStopEvent() throws Exception {
        subject.consumeStateChange(stateChangedEvent(STOP));

        verify(jobServiceMock).stopJob(JOB_ID);
    }

    @Test
    public void shouldPersistMessage() throws Exception {
        MessageEvent messageEvent = newMessageEvent(jobRunnableMock, JOB_ID, MessageEvent.Level.INFO, "some message");
        OffsetDateTime timestamp = OffsetDateTime.ofInstant(Instant.ofEpochMilli(messageEvent.getTimestamp()), ZoneId.systemDefault());

        subject.consumeMessage(messageEvent);

        verify(jobServiceMock).appendMessage(JOB_ID, JobMessage.jobMessage(Level.INFO, "some message", timestamp));
    }

    @Test
    public void shouldNotThrowIfSomethingFailsInDatabase() {
        MessageEvent messageEvent = newMessageEvent(jobRunnableMock, JOB_ID, MessageEvent.Level.INFO, "some message");
        OffsetDateTime timestamp = OffsetDateTime.ofInstant(Instant.ofEpochMilli(messageEvent.getTimestamp()), ZoneId.systemDefault());
        final JobMessage expectedJobMessage = JobMessage.jobMessage(Level.INFO, "some message", timestamp);
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
