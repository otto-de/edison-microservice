package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import net.jcip.annotations.Immutable;
import org.springframework.context.ApplicationEventPublisher;

import java.net.URI;

import static de.otto.edison.jobs.eventbus.events.MessageEvent.newMessageEvent;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.newStateChangeEvent;

@Immutable
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final Object source;
    private final URI jobUri;
    private final String jobType;

    private EventPublisher(final ApplicationEventPublisher applicationEventPublisher,
                           final Object source,
                           final URI jobUri,
                           final String jobType) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.source = source;
        this.jobUri = jobUri;
        this.jobType = jobType;
    }

    public void stateChanged(final StateChangeEvent.State state) {
        applicationEventPublisher.publishEvent(newStateChangeEvent(source, jobUri, jobType, state));
    }

    public void message(final MessageEvent.Level level, final String message) {
        applicationEventPublisher.publishEvent(newMessageEvent(source, jobUri, level, message));
    }

    public static EventPublisher newJobEventPublisher(final ApplicationEventPublisher applicationEventPublisher,
                                                      final Object source,
                                                      final URI jobUri,
                                                      final String jobType) {
        return new EventPublisher(
                applicationEventPublisher,
                source,
                jobUri,
                jobType
        );
    }
}
