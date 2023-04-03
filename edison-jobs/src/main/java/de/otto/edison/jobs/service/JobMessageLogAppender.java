package de.otto.edison.jobs.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import de.otto.edison.jobs.domain.JobMarker;
import de.otto.edison.jobs.domain.JobMessage;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;

import static de.otto.edison.jobs.domain.JobMessage.jobMessage;

/**
 * Logback log appender that publishes logging events as application events, when a job_id is set in MDC,
 * so that log messages are displayed in the job message html page.
 */
@Component
public class JobMessageLogAppender extends AppenderBase<ILoggingEvent> {

    private final JobService jobService;

    @Autowired
    public JobMessageLogAppender(final JobService jobService) {
        this.jobService = jobService;

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        setContext(lc);
        start();

        lc.getLogger("ROOT").addAppender(this);
    }

    @Override
    protected void append(final ILoggingEvent eventObject) {
        Map<String, String> mdcMap = eventObject.getMDCPropertyMap();
        // TODO: check for JOB marker:
        if (mdcMap.containsKey("job_id") && eventObject.getMarkerList() != null && eventObject.getMarkerList().contains(JobMarker.JOB)) {
            String jobId = mdcMap.get("job_id");
            Level level = eventObject.getLevel();
            de.otto.edison.jobs.domain.Level edisonLevel = logLevelToEdisonLevel(level);

            String message = eventObject.getFormattedMessage();

            try {
                final JobMessage jobMessage = jobMessage(edisonLevel, message, OffsetDateTime.now());
                jobService.appendMessage(jobId, jobMessage);
            }
            catch(final RuntimeException e) {
                addError("Failed to persist job message (jobId=" + jobId + "): " + message, e);
            }
        }
    }

    private de.otto.edison.jobs.domain.Level logLevelToEdisonLevel(final Level level) {
        return switch (level.levelStr) {
            case "ERROR" -> de.otto.edison.jobs.domain.Level.ERROR;
            case "WARN" -> de.otto.edison.jobs.domain.Level.WARNING;
            default -> de.otto.edison.jobs.domain.Level.INFO;
        };
    }
}
