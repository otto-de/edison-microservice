package de.otto.edison.jobs.eventbus.events;

import net.jcip.annotations.Immutable;
import org.springframework.context.ApplicationEvent;

import java.net.URI;

@Immutable
public class StateChangeEvent extends ApplicationEvent {

    private final URI jobUri;
    private final State state;

    private StateChangeEvent(final Object source, final URI jobUri, final State state) {
        super(source);
        this.jobUri = jobUri;
        this.state = state;
    }

    public URI getJobUri() {
        return jobUri;
    }

    public State getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateChangeEvent that = (StateChangeEvent) o;

        if (jobUri != null ? !jobUri.equals(that.jobUri) : that.jobUri != null) return false;
        return state == that.state;

    }

    @Override
    public int hashCode() {
        int result = jobUri != null ? jobUri.hashCode() : 0;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StateChangeEvent{" +
                "jobUri=" + jobUri +
                ", state=" + state +
                '}';
    }

    public static StateChangeEvent newStateChangeEvent(final Object source, final URI jobUri, final State state) {
        return new StateChangeEvent(source, jobUri, state);
    }

    public enum State {
        START,
        STOP,
        STILL_ALIVE
    }
}
