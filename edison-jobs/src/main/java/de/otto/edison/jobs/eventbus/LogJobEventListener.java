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
        String msg = String.format("'%s': '%s'", messageEvent.getMessage(), messageEvent.getJobId());
        switch (messageEvent.getLevel()) {
            case INFO:
                if (messageEvent.getMarker().isPresent()) {
                    LOG.info(messageEvent.getMarker().get(), msg);
                } else {
                    LOG.info(msg);
                }
                break;
            case WARNING:
                if (messageEvent.getMarker().isPresent()) {
                    LOG.warn(messageEvent.getMarker().get(), msg);
                } else {
                    LOG.warn(msg);
                }
                break;
            case ERROR:
                if (messageEvent.getMarker().isPresent()) {
                    LOG.error(messageEvent.getMarker().get(), msg);
                } else {
                    LOG.error(msg);
                }
        }
    }
}
