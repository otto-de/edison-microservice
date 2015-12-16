package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.service.JobRunnable;
import org.springframework.context.ApplicationContext;

import java.net.URI;

import static de.otto.edison.jobs.eventbus.events.ErrorEvent.newErrorEvent;
import static de.otto.edison.jobs.eventbus.events.InfoEvent.newInfoEvent;
import static de.otto.edison.jobs.eventbus.events.PingEvent.newPingEvent;
import static de.otto.edison.jobs.eventbus.events.StartedEvent.newStartedEvent;
import static de.otto.edison.jobs.eventbus.events.StoppedEvent.newStoppedEvent;

public class EventPublisher {

    private final ApplicationContext applicationContext;

    public EventPublisher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void started(final Object source, final URI jobUri) {
        applicationContext.publishEvent(newStartedEvent(source, jobUri));
    }

    public void stopped(final Object source, final URI jobUri) {
        applicationContext.publishEvent(newStoppedEvent(source, jobUri));
    }

    public void info(final Object source, final URI jobUri, final String message) {
        applicationContext.publishEvent(newInfoEvent(source, jobUri, message));
    }

    public void error(final Object source, final URI jobUri, final String message) {
        applicationContext.publishEvent(newErrorEvent(source, jobUri, message));
    }

    public void ping(final Object source, final URI jobUri) {
        applicationContext.publishEvent(newPingEvent(source, jobUri));
    }
}
