package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.*;
import org.springframework.context.event.EventListener;

public interface JobEventListener {

    @EventListener
    void consumeStateChange(StateChangeEvent stateChangeEvent);

    @EventListener
    void consumeMessage(MessageEvent messageEvent);
}
