package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.*;

public interface JobEventListener {

    void consumeStateChange(StateChangeEvent stateChangeEvent);

    void consumeMessage(MessageEvent messageEvent);
}
