package de.otto.edison.jobs.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import de.otto.edison.jobs.domain.JobMarker;
import de.otto.edison.jobs.domain.JobMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singletonMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class JobMessageLogAppenderTest {

    private JobService jobService;

    private JobMessageLogAppender jobEventAppender;

    @Before
    public void setUp() throws Exception {
        jobService = mock(JobService.class);
        jobEventAppender = new JobMessageLogAppender(jobService);
    }

    @Test
    public void shouldNotLogWhenNoJobIdInMDC() throws Exception {
        final LoggingEvent loggingEvent = new LoggingEvent();

        //when
        jobEventAppender.append(loggingEvent);

        //then
        verifyZeroInteractions(jobService);
    }

    @Test
    public void shouldStartAppender() throws Exception {
        assertThat(jobEventAppender.isStarted(), is(true));
    }

    @Test
    public void shouldPublishEventWithJobIdAndLevelERROR() throws Exception {
        // given
        final LoggingEvent loggingEvent = createLoggingEvent(Level.ERROR);

        // when
        jobEventAppender.append(loggingEvent);

        // then
        final ArgumentCaptor<JobMessage> messageCaptor = forClass(JobMessage.class);
        verify(jobService).appendMessage(eq("someJobId"), messageCaptor.capture());

        assertMessageEvent(messageCaptor, de.otto.edison.jobs.domain.Level.ERROR);
    }

    @Test
    public void shouldPublishEventWithJobIdAndLevelWARN() throws Exception {
        // given
        final LoggingEvent loggingEvent = createLoggingEvent(Level.WARN);

        // when
        jobEventAppender.append(loggingEvent);

        // then
        final ArgumentCaptor<JobMessage> messageCaptor = forClass(JobMessage.class);
        verify(jobService).appendMessage(eq("someJobId"), messageCaptor.capture());

        assertMessageEvent(messageCaptor, de.otto.edison.jobs.domain.Level.WARNING);
    }

    @Test
    public void shouldPublishEventWithJobIdAndLevelINFO() throws Exception {
        // given
        final LoggingEvent loggingEvent = createLoggingEvent(Level.INFO);

        // when
        jobEventAppender.append(loggingEvent);

        // then
        final ArgumentCaptor<JobMessage> messageCaptor = forClass(JobMessage.class);
        verify(jobService).appendMessage(eq("someJobId"), messageCaptor.capture());

        assertMessageEvent(messageCaptor, de.otto.edison.jobs.domain.Level.INFO);
    }

    private void assertMessageEvent(final ArgumentCaptor<JobMessage> messageCaptor,
                                    final de.otto.edison.jobs.domain.Level expectedLevel) {
        final JobMessage jobMessage = messageCaptor.getValue();
        assertThat(jobMessage.getMessage(), is("someMessage"));
        assertThat(jobMessage.getLevel(), is(expectedLevel));
    }

    private LoggingEvent createLoggingEvent(final Level level) {
        return createLoggingEvent(level, "someMessage");
    }

    private LoggingEvent createLoggingEvent(final Level level,
                                            final String message,
                                            final Object... params) {
        final LoggingEvent loggingEvent = new LoggingEvent();
        loggingEvent.setMDCPropertyMap(singletonMap("job_id", "someJobId"));
        loggingEvent.setMessage(message);
        loggingEvent.setArgumentArray(params);
        loggingEvent.setLevel(level);
        loggingEvent.setMarker(JobMarker.JOB);
        return loggingEvent;
    }
}