package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.StartedEvent;
import de.otto.edison.jobs.eventbus.events.StoppedEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

public class InMemoryEventRubbishBin {

    private final List<String> startEvents = new ArrayList<>();
    private final List<String> stoppedEvents = new ArrayList<>();

    @EventListener
    public void consume(final StartedEvent startedEvent) {
        startEvents.add(startedEvent.getJobUri().toString());
    }

    @EventListener
    public void consume(final StoppedEvent stoppedEvent) {
        stoppedEvents.add(stoppedEvent.getJobUri().toString());
    }

    public List<String> getStartedEvents() {
        return startEvents;
    }

    public List<String> getStoppedEvents() {
        return stoppedEvents;
    }

    public void clear() {
        startEvents.clear();
    }
}
