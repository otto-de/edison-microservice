package de.otto.edison.jobs.eventbus.events;

import de.otto.edison.jobs.service.JobRunnable;
import net.jcip.annotations.Immutable;
import org.springframework.context.ApplicationEvent;

import java.net.URI;

@Immutable
public class InfoEvent extends ApplicationEvent {

    private final URI jobUri;
    private final String message;

    public InfoEvent(Object source, URI jobUri, String message) {
        super(source);
        this.jobUri = jobUri;
        this.message = message;
    }

    public URI getJobUri() {
        return jobUri;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InfoEvent that = (InfoEvent) o;

        if (jobUri != null ? !jobUri.equals(that.jobUri) : that.jobUri != null) return false;
        return !(message != null ? !message.equals(that.message) : that.message != null);

    }

    @Override
    public int hashCode() {
        int result = jobUri != null ? jobUri.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InfoEvent{" +
                "jobUri=" + jobUri +
                ", message='" + message + '\'' +
                '}';
    }

    public static InfoEvent newInfoEvent(final Object source, final URI jobUri, final String message) {
        return new InfoEvent(source, jobUri, message);
    }
}
