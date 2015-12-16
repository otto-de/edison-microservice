package de.otto.edison.jobs.eventbus.events;

import net.jcip.annotations.Immutable;
import org.springframework.context.ApplicationEvent;

import java.net.URI;

@Immutable
public class StateChangeEvent extends ApplicationEvent {

    private final URI jobUri;
    private final String jobType;
    private final State state;

    private StateChangeEvent(final Object source,
                             final URI jobUri,
                             final String jobType,
                             final State state) {
        super(source);
        this.jobUri = jobUri;
        this.jobType = jobType;
        this.state = state;
    }

    public URI getJobUri() {
        return jobUri;
    }

    public State getState() {
        return state;
    }

    public String getJobType() {
        return jobType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateChangeEvent that = (StateChangeEvent) o;

        if (jobUri != null ? !jobUri.equals(that.jobUri) : that.jobUri != null) return false;
        if (jobType != null ? !jobType.equals(that.jobType) : that.jobType != null) return false;
        return state == that.state;

    }

    @Override
    public int hashCode() {
        int result = jobUri != null ? jobUri.hashCode() : 0;
        result = 31 * result + (jobType != null ? jobType.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StateChangeEvent{" +
                "jobUri=" + jobUri +
                ", jobType='" + jobType + '\'' +
                ", state=" + state +
                '}';
    }

    public static StateChangeEvent newStateChangeEvent(final Object source,
                                                       final URI jobUri,
                                                       final String jobType,
                                                       final State state) {
        return new StateChangeEvent(source, jobUri, jobType, state);
    }

    public enum State {
        CREATE,
        START,
        STOP,
        RESTART,
        STILL_ALIVE,
        DEAD
    }
}
