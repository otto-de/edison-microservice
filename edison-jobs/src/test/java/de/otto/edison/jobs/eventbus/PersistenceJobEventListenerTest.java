package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import de.otto.edison.jobs.repository.JobRepository;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Clock;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.eventbus.events.MessageEvent.newMessageEvent;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.*;
import static java.time.Clock.fixed;
import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
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
        verify(jobRepository).createOrUpdate(newJobInfo(new URI("some/job"), "someJobType", clock));
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

    @Test
    public void shouldPersistDeadEvent() throws Exception {
        // given
        StateChangeEvent stateChangeEvent = StateChangeEvent.newStateChangeEvent(this, URI.create("some/job"), "someJobType", DEAD);
        JobInfo jobInfo = mock(JobInfo.class);
        when(jobRepository.findOne(URI.create("some/job"))).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeStateChange(stateChangeEvent);

        // then
        verify(jobRepository).findOne(URI.create("some/job"));
        verify(jobInfo).dead();
        verify(jobRepository).createOrUpdate(jobInfo);
    }

    @Test
    public void shouldPersistStopEvent() throws Exception {
        // given
        StateChangeEvent stateChangeEvent = StateChangeEvent.newStateChangeEvent(this, URI.create("some/job"), "someJobType", STOP);
        JobInfo jobInfo = mock(JobInfo.class);
        when(jobRepository.findOne(URI.create("some/job"))).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeStateChange(stateChangeEvent);

        // then
        verify(jobRepository).findOne(URI.create("some/job"));
        verify(jobInfo).stop();
        verify(jobRepository).createOrUpdate(jobInfo);
    }

    @Test
    public void shouldPersistInfoMessages() throws Exception {
        // given
        MessageEvent messageEvent = newMessageEvent(this, URI.create("some/job"), MessageEvent.Level.INFO, "some message");
        JobInfo jobInfo = JobInfo.newJobInfo(URI.create("some/job"), "someType", Clock.systemDefaultZone());
        when(jobRepository.findOne(URI.create("some/job"))).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeMessage(messageEvent);

        // then
        assertThat(jobInfo.getMessages(), hasSize(1));
        assertThat(jobInfo.getMessages().get(0).getLevel(), is(Level.INFO));
        assertThat(jobInfo.getMessages().get(0).getMessage(), is("some message"));

        verify(jobRepository).findOne(URI.create("some/job"));
        verify(jobRepository).createOrUpdate(jobInfo);
    }

    @Test
    public void shouldPersistWarnMessages() throws Exception {
        // given
        MessageEvent messageEvent = newMessageEvent(this, URI.create("some/job"), MessageEvent.Level.WARN, "some message");
        JobInfo jobInfo = JobInfo.newJobInfo(URI.create("some/job"), "someType", Clock.systemDefaultZone());
        when(jobRepository.findOne(URI.create("some/job"))).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeMessage(messageEvent);

        // then
        assertThat(jobInfo.getMessages(), hasSize(1));
        assertThat(jobInfo.getMessages().get(0).getLevel(), is(Level.WARNING));
        assertThat(jobInfo.getMessages().get(0).getMessage(), is("some message"));

        verify(jobRepository).findOne(URI.create("some/job"));
        verify(jobRepository).createOrUpdate(jobInfo);
    }

    @Test
    public void shouldPersistErrorMessages() throws Exception {
        // given
        MessageEvent messageEvent = newMessageEvent(this, URI.create("some/job"), MessageEvent.Level.ERROR, "some message");
        JobInfo jobInfo = JobInfo.newJobInfo(URI.create("some/job"), "someType", Clock.systemDefaultZone());
        when(jobRepository.findOne(URI.create("some/job"))).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeMessage(messageEvent);

        // then
        assertThat(jobInfo.getMessages(), hasSize(1));
        assertThat(jobInfo.getMessages().get(0).getLevel(), is(Level.ERROR));
        assertThat(jobInfo.getMessages().get(0).getMessage(), is("some message"));

        verify(jobRepository).findOne(URI.create("some/job"));
        verify(jobRepository).createOrUpdate(jobInfo);
    }
}
