package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

public class LogJobEventListener implements JobEventListener {

    public static final Logger LOG = LoggerFactory.getLogger(LogJobEventListener.class);

    @Override
    @EventListener
    public void consumeStarted(final StartedEvent startedEvent) {
        LOG.info("job '{}' started", startedEvent.getJobUri());
    }

    @Override
    @EventListener
    public void consumeStopped(final StoppedEvent stoppedEvent) {
        LOG.info("job '{}' stopped", stoppedEvent.getJobUri());
    }

    @Override
    @EventListener
    public void consumeError(final ErrorEvent errorEvent) {
        LOG.error("error '{}' in job '{}'", errorEvent.getMessage(), errorEvent.getJobUri());
    }

    @Override
    @EventListener
    public void consumeInfo(final InfoEvent infoEvent) {
        LOG.error("'{}': '{}'", infoEvent.getMessage(), infoEvent.getJobUri());
    }

    @Override
    @EventListener
    public void consumePing(final PingEvent pingEvent) {
        LOG.info("job '{}' pings", pingEvent.getJobUri());
    }
}
