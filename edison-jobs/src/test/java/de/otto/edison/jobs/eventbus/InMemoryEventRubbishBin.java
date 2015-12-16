package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

public class InMemoryEventRubbishBin {

    private final List<String> stateChangedEvents = new ArrayList<>();
    private final List<String> messageEvents = new ArrayList<>();

    @EventListener
    public void consumeStateChangedEvent(final StateChangeEvent stateChangeEvent) {
        stateChangedEvents.add(stateChangeEvent.getJobUri().toString());
    }

    @EventListener
    public void consumeMessageEvent(final MessageEvent messageEvent) {
        messageEvents.add(messageEvent.getJobUri().toString());
    }

    public List<String> getStateChangedEvents() {
        return stateChangedEvents;
    }

    public List<String> getMessageEvents() {
        return messageEvents;
    }

    public void clear() {
        stateChangedEvents.clear();
        messageEvents.clear();
    }
}
