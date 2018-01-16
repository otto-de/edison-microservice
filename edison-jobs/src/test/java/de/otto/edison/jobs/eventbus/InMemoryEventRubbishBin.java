package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

public class InMemoryEventRubbishBin {

    private final List<String> stateChangedEvents = new ArrayList<>();

    @EventListener
    public void consumeStateChangedEvent(final StateChangeEvent stateChangeEvent) {
        stateChangedEvents.add(stateChangeEvent.getJobId());
    }
    public List<String> getStateChangedEvents() {
        return stateChangedEvents;
    }

    public void clear() {
        stateChangedEvents.clear();
    }
}
