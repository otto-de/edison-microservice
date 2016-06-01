package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogJobEventListener implements JobEventListener {

    public static final Logger LOG = LoggerFactory.getLogger(LogJobEventListener.class);

    @Override
    public void consumeStateChange(final StateChangeEvent stateChangeEvent) {
        LOG.info("jobType='{}' state changed to '{}' ('{}')", stateChangeEvent.getJobType(),
                stateChangeEvent.getState(), stateChangeEvent.getJobId());
    }

    @Override
    public void consumeMessage(final MessageEvent messageEvent) {
        switch (messageEvent.getLevel()) {
            case INFO:
                LOG.info("'{}': '{}'", messageEvent.getMessage(), messageEvent.getJobId());
                break;
            case WARN:
                LOG.warn("'{}': '{}'", messageEvent.getMessage(), messageEvent.getJobId());
                break;
            case ERROR:
                LOG.error("'{}': '{}'", messageEvent.getMessage(), messageEvent.getJobId());
        }
    }
}
