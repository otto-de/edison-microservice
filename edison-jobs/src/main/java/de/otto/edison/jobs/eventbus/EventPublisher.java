package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import org.springframework.context.ApplicationContext;

import java.net.URI;

import static de.otto.edison.jobs.eventbus.events.MessageEvent.newMessageEvent;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.newStateChangeEvent;

public class EventPublisher {

    private final ApplicationContext applicationContext;

    public EventPublisher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void stateChanged(final Object source, final URI jobUri, final StateChangeEvent.State state) {
        applicationContext.publishEvent(newStateChangeEvent(source, jobUri, state));
    }

    public void message(final Object source, final URI jobUri, final MessageEvent.Level level, final String message) {
        applicationContext.publishEvent(newMessageEvent(source, jobUri, level, message));
    }
}
