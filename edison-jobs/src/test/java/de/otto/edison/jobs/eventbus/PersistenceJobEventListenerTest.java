package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.service.JobRunnable;
import de.otto.edison.status.domain.SystemInfo;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Clock;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.eventbus.events.MessageEvent.newMessageEvent;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.*;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.newStateChangeEvent;
import static java.time.Clock.fixed;
import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@Test
public class PersistenceJobEventListenerTest {

    private PersistenceJobEventListener testee;
    private JobRepository jobRepository;
    private Clock clock;
    private SystemInfo systemInfo;

    @BeforeMethod
    public void setUp() throws Exception {
        jobRepository = mock(JobRepository.class);
        clock = fixed(now(), systemDefault());
        systemInfo = SystemInfo.systemInfo("localhost", 8080);

        testee = new PersistenceJobEventListener(jobRepository, clock, systemInfo);
    }

    @Test
    public void shouldPersistStartEvent() throws Exception {
        // given
        StateChangeEvent stateChangeEvent = newStateChangeEvent(someJobRunnable(), "some/job", START);

        // when
        testee.consumeStateChange(stateChangeEvent);

        // then
        verify(jobRepository).createOrUpdate(newJobInfo("some/job", "someJobType", clock, "localhost"));
    }

    @Test
    public void shouldPersistStillAliveEvent() throws Exception {
        // given
        StateChangeEvent stateChangeEvent = newStateChangeEvent(someJobRunnable(), "some/job", KEEP_ALIVE);
        JobInfo jobInfo = mock(JobInfo.class);
        when(jobRepository.findOne("some/job")).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeStateChange(stateChangeEvent);

        // then
        verify(jobRepository).findOne("some/job");
        verify(jobInfo).ping();
        verify(jobRepository).createOrUpdate(jobInfo);
    }

    @Test
    public void shouldPersistRestartEvent() throws Exception {
        // given
        StateChangeEvent stateChangeEvent = newStateChangeEvent(someJobRunnable(), "some/job", RESTART);
        JobInfo jobInfo = mock(JobInfo.class);
        when(jobRepository.findOne("some/job")).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeStateChange(stateChangeEvent);

        // then
        verify(jobRepository).findOne("some/job");
        verify(jobInfo).restart();
        verify(jobRepository).createOrUpdate(jobInfo);
    }

    @Test
    public void shouldPersistDeadEvent() throws Exception {
        // given
        StateChangeEvent stateChangeEvent = newStateChangeEvent(someJobRunnable(), "some/job", DEAD);
        JobInfo jobInfo = mock(JobInfo.class);
        when(jobRepository.findOne("some/job")).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeStateChange(stateChangeEvent);

        // then
        verify(jobRepository).findOne("some/job");
        verify(jobInfo).dead();
        verify(jobRepository).createOrUpdate(jobInfo);
    }

    @Test
    public void shouldPersistStopEvent() throws Exception {
        // given
        StateChangeEvent stateChangeEvent = newStateChangeEvent(someJobRunnable(), "some/job", STOP);
        JobInfo jobInfo = mock(JobInfo.class);
        when(jobRepository.findOne("some/job")).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeStateChange(stateChangeEvent);

        // then
        verify(jobRepository).findOne("some/job");
        verify(jobInfo).stop();
        verify(jobRepository).createOrUpdate(jobInfo);
    }

    @Test
    public void shouldPersistInfoMessages() throws Exception {
        // given
        MessageEvent messageEvent = newMessageEvent(someJobRunnable(), "some/job", MessageEvent.Level.INFO, "some message");
        JobInfo jobInfo = JobInfo.newJobInfo("some/job", "someType", Clock.systemDefaultZone(), "localhost");
        when(jobRepository.findOne("some/job")).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeMessage(messageEvent);

        // then
        ArgumentCaptor<JobMessage> captor = ArgumentCaptor.forClass(JobMessage.class);
        verify(jobRepository).appendMessage(eq("some/job"), captor.capture());
        assertThat(captor.getValue().getLevel(), is(Level.INFO));
        assertThat(captor.getValue().getMessage(), is("some message"));
        verifyNoMoreInteractions(jobRepository);
    }

    @Test
    public void shouldPersistWarnMessages() throws Exception {
        // given
        MessageEvent messageEvent = newMessageEvent(someJobRunnable(), "some/job", MessageEvent.Level.WARN, "some message");
        JobInfo jobInfo = JobInfo.newJobInfo("some/job", "someType", Clock.systemDefaultZone(), "localhost");
        when(jobRepository.findOne("some/job")).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeMessage(messageEvent);

        // then
        ArgumentCaptor<JobMessage> captor = ArgumentCaptor.forClass(JobMessage.class);
        verify(jobRepository).appendMessage(eq("some/job"), captor.capture());
        assertThat(captor.getValue().getLevel(), is(Level.WARNING));
        assertThat(captor.getValue().getMessage(), is("some message"));
        verifyNoMoreInteractions(jobRepository);
    }

    @Test
    public void shouldPersistErrorMessages() throws Exception {
        // given
        MessageEvent messageEvent = newMessageEvent(someJobRunnable(), "some/job", MessageEvent.Level.ERROR, "some message");
        JobInfo jobInfo = mock(JobInfo.class);
        when(jobRepository.findOne(messageEvent.getJobId())).thenReturn(Optional.of(jobInfo));

        // when
        testee.consumeMessage(messageEvent);

        // then
        verify(jobRepository).createOrUpdate(jobInfo);
        verify(jobInfo).error(any());
    }

    private JobRunnable someJobRunnable() {
        return new JobRunnable() {
            @Override
            public JobDefinition getJobDefinition() {
                return new JobDefinition() {
                    @Override
                    public String jobType() {
                        return "someJobType";
                    }

                    @Override
                    public String jobName() {
                        return "someName";
                    }

                    @Override
                    public String description() {
                        return "";
                    }
                };
            }

            @Override
            public void execute(JobEventPublisher jobEventPublisher) {
            }
        };
    }
}
