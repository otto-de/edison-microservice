package de.otto.edison.jobs.eventbus.events;

import de.otto.edison.jobs.service.JobRunnable;
import net.jcip.annotations.Immutable;
import org.springframework.context.ApplicationEvent;

import java.net.URI;

@Immutable
public class StoppedEvent extends ApplicationEvent {

    private final URI jobUri;

    private StoppedEvent(final Object source, final URI jobUri) {
        super(source);
        this.jobUri = jobUri;
    }

    public URI getJobUri() {
        return jobUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoppedEvent that = (StoppedEvent) o;

        return !(jobUri != null ? !jobUri.equals(that.jobUri) : that.jobUri != null);

    }

    @Override
    public int hashCode() {
        return jobUri != null ? jobUri.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "StoppedEvent{" +
                "jobUri=" + jobUri +
                '}';
    }

    public static StoppedEvent newStoppedEvent(final Object source, final URI jobUri) {
        return new StoppedEvent(source, jobUri);
    }
}
