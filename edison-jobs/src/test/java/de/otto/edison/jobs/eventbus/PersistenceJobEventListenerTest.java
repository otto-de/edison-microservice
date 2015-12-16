package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import de.otto.edison.jobs.repository.JobRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Clock;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.*;
import static java.time.Clock.fixed;
import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static org.mockito.Mockito.*;

@Test
public class PersistenceJobEventListenerTest {

    private PersistenceJobEventListener testee;
    private JobRepository jobRepository;
    private Clock clock;

    @BeforeMethod
    public void setUp() throws Exception {
        jobRepository = mock(JobRepository.class);
        clock = fixed(now(), systemDefault());

        testee = new PersistenceJobEventListener(jobRepository, clock);
    }

    @Test
    public void shouldPersistCreateEvent() throws Exception {
        // given
        StateChangeEvent stateChangeEvent = StateChangeEvent.newStateChangeEvent(this, URI.create("some/job"), "someJobType", CREATE);

        // when
        testee.consumeStateChange(stateChangeEvent);

        // then
        verify(jobRepository).createOrUpdate(newJobInfo(new URI("some/job"), "someJobType", null, clock));
    }

    @Test
    public void shouldPersistStillAliveEvent() throws Exception {
        // given
        StateChangeEvent stateChangeEvent = StateChangeEvent.newStateChangeEvent(this, URI.create("some/job"), "someJobType", STILL_ALIVE);
        JobInfo jobInfo = mock(JobInfo.class);
        when(jobRepository.findOne(URI.create("some/job"))).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeStateChange(stateChangeEvent);

        // then
        verify(jobRepository).findOne(URI.create("some/job"));
        verify(jobInfo).ping();
        verify(jobRepository).createOrUpdate(jobInfo);
    }

    @Test
    public void shouldPersistRestartEvent() throws Exception {
        // given
        StateChangeEvent stateChangeEvent = StateChangeEvent.newStateChangeEvent(this, URI.create("some/job"), "someJobType", RESTART);
        JobInfo jobInfo = mock(JobInfo.class);
        when(jobRepository.findOne(URI.create("some/job"))).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeStateChange(stateChangeEvent);

        // then
        verify(jobRepository).findOne(URI.create("some/job"));
        verify(jobInfo).restart();
        verify(jobRepository).createOrUpdate(jobInfo);
    }
}
