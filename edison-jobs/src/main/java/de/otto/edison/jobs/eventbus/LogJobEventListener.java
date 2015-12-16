package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

public class LogJobEventListener implements JobEventListener {

    public static final Logger LOG = LoggerFactory.getLogger(LogJobEventListener.class);

    @Override
    @EventListener
    public void consumeStateChange(final StateChangeEvent stateChangeEvent) {
        LOG.info("job state changed to '{}'", stateChangeEvent.getJobUri(), stateChangeEvent.getState());
    }

    @Override
    @EventListener
    public void consumeMessage(final MessageEvent messageEvent) {
        switch (messageEvent.getLevel()) {
            case INFO:
                LOG.info("'{}': '{}'", messageEvent.getMessage(), messageEvent.getJobUri());
                break;
            case WARN:
                LOG.warn("'{}': '{}'", messageEvent.getMessage(), messageEvent.getJobUri());
                break;
            case ERROR:
                LOG.error("'{}': '{}'", messageEvent.getMessage(), messageEvent.getJobUri());
        }
    }
}
