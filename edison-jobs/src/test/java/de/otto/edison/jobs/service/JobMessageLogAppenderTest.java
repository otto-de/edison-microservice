package de.otto.edison.jobs.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import de.otto.edison.jobs.domain.JobMarker;
import de.otto.edison.jobs.domain.JobMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class JobMessageLogAppenderTest {

    private JobService jobService;

    private JobMessageLogAppender jobEventAppender;

    @BeforeEach
    public void setUp() {
        jobService = mock(JobService.class);
        jobEventAppender = new JobMessageLogAppender(jobService);
    }

    @Test
    public void shouldNotLogWhenNoJobIdInMDC() {
        final LoggingEvent loggingEvent = new LoggingEvent();
        final LoggerContext loggerContext = new LoggerContext();
        loggerContext.setMDCAdapter((LogbackMDCAdapter) MDC.getMDCAdapter());
        loggingEvent.setLoggerContext(loggerContext);

        //when
        jobEventAppender.append(loggingEvent);

        //then
        verifyNoInteractions(jobService);
    }

    @Test
    public void shouldStartAppender() {
        assertThat(jobEventAppender.isStarted(), is(true));
    }

    @Test
    public void shouldPublishEventWithJobIdAndLevelERROR() {
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
    public void shouldPublishEventWithJobIdAndLevelWARN() {
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
    public void shouldPublishEventWithJobIdAndLevelINFO() {
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
        loggingEvent.addMarker(JobMarker.JOB);
        return loggingEvent;
    }
}
