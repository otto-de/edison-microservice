package de.otto.edison.jobs.eventbus.events;

import de.otto.edison.jobs.service.JobRunnable;
import net.jcip.annotations.Immutable;
import org.springframework.context.ApplicationEvent;

import java.net.URI;

@Immutable
public class StartedEvent extends ApplicationEvent {

    private final URI jobUri;

    private StartedEvent(final Object source, final URI jobUri) {
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

        StartedEvent that = (StartedEvent) o;

        return !(jobUri != null ? !jobUri.equals(that.jobUri) : that.jobUri != null);

    }

    @Override
    public int hashCode() {
        return jobUri != null ? jobUri.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "StartedEvent{" +
                "jobUri=" + jobUri +
                '}';
    }

    public static StartedEvent newStartedEvent(final Object source, final URI jobUri) {
        return new StartedEvent(source, jobUri);
    }
}
