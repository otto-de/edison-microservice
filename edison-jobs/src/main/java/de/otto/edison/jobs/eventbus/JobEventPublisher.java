package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.MessageEvent;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent;
import net.jcip.annotations.Immutable;
import org.springframework.context.ApplicationEventPublisher;

import java.net.URI;

import static de.otto.edison.jobs.eventbus.events.MessageEvent.newMessageEvent;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.newStateChangeEvent;

@Immutable
public class JobEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final Object source;
    private final URI jobUri;
    private final String jobType;

    private JobEventPublisher(final ApplicationEventPublisher applicationEventPublisher,
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

    public static JobEventPublisher newJobEventPublisher(final ApplicationEventPublisher applicationEventPublisher,
                                                         final Object source,
                                                         final URI jobUri,
                                                         final String jobType) {
        return new JobEventPublisher(
                applicationEventPublisher,
                source,
                jobUri,
                jobType
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobEventPublisher that = (JobEventPublisher) o;

        if (applicationEventPublisher != null ? !applicationEventPublisher.equals(that.applicationEventPublisher) : that.applicationEventPublisher != null)
            return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (jobUri != null ? !jobUri.equals(that.jobUri) : that.jobUri != null) return false;
        return !(jobType != null ? !jobType.equals(that.jobType) : that.jobType != null);

    }

    @Override
    public int hashCode() {
        int result = applicationEventPublisher != null ? applicationEventPublisher.hashCode() : 0;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (jobUri != null ? jobUri.hashCode() : 0);
        result = 31 * result + (jobType != null ? jobType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JobEventPublisher{" +
                "applicationEventPublisher=" + applicationEventPublisher +
                ", source=" + source +
                ", jobUri=" + jobUri +
                ", jobType='" + jobType + '\'' +
                '}';
    }
}
