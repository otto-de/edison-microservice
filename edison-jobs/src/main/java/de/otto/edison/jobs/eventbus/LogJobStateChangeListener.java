package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.otto.edison.jobs.domain.Level.*;

public class LogJobStateChangeListener implements JobStateChangeListener {

    public static final Logger LOG = LoggerFactory.getLogger(LogJobStateChangeListener.class);

    @Override
    public void consumeStateChange(final StateChangeEvent stateChangeEvent) {
        LOG.info(
                "jobType='{}' state changed to '{}' ('{}'): {}",
                stateChangeEvent.getJobType(),
                stateChangeEvent.getState(),
                stateChangeEvent.getJobId(),
                stateChangeEvent.getMessage());
    }

    @Override
    public void consumeMessage(final MessageEvent messageEvent) {
        if (messageEvent.getLevel() == INFO) {
            LOG.info("message='{}'", messageEvent.getMessage());
        }
        if (messageEvent.getLevel() == WARNING) {
            LOG.warn("message='{}'", messageEvent.getMessage());
        }
        if (messageEvent.getLevel() == ERROR) {
            LOG.error("message='{}'", messageEvent.getMessage());
        }
    }

}
