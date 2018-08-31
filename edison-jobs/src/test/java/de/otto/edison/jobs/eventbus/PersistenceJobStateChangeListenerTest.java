package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import de.otto.edison.jobs.service.JobRunnable;
import de.otto.edison.jobs.service.JobService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.OffsetDateTime;

import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.domain.Level.ERROR;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.DEAD;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.FAILED;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.KEEP_ALIVE;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.RESTART;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.SKIPPED;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.STOP;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.newStateChangeEvent;
import static java.time.Instant.ofEpochMilli;
import static java.time.OffsetDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PersistenceJobStateChangeListenerTest {

    private static final String JOB_ID = "some/job/id";
    private static final String JOB_TYPE = "jobType";

    @Mock
    private JobService jobServiceMock;
    @Mock
    private JobRunnable jobRunnableMock;

    private PersistenceJobStateChangeListener subject;



    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(jobRunnableMock.getJobDefinition()).thenReturn(mock(JobDefinition.class));

        subject = new PersistenceJobStateChangeListener(jobServiceMock);
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

        verify(jobServiceMock).killJob(JOB_ID);
    }

    @Test
    public void shouldPersistStopEvent() throws Exception {
        subject.consumeStateChange(stateChangedEvent(STOP));

        verify(jobServiceMock).stopJob(JOB_ID);
    }

    @Test
    public void shouldPersistFailedEvent() throws Exception {
        final StateChangeEvent event = stateChangedEvent(FAILED);
        subject.consumeStateChange(event);

        final OffsetDateTime ts = ofInstant(ofEpochMilli(event.getTimestamp()), systemDefault());
        verify(jobServiceMock).appendMessage(JOB_ID, jobMessage(ERROR, "", ts));
    }

    @Test
    public void shouldPersistSkippedEvent() throws Exception {
        subject.consumeStateChange(stateChangedEvent(SKIPPED));

        verify(jobServiceMock).markSkipped(JOB_ID);
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
