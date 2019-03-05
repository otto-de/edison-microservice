package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import org.springframework.context.event.EventListener;

public interface JobStateChangeListener {

    @EventListener
    void consumeStateChange(StateChangeEvent stateChangeEvent);

    @EventListener
    void consumeMessage(MessageEvent messageEvent);
}
