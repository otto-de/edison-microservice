package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.eventbus.events.MessageEvent.Level;
import de.otto.edison.jobs.eventbus.events.StateChangeEvent.State;
import de.otto.edison.jobs.service.JobRunnable;
import net.jcip.annotations.Immutable;
import org.springframework.context.ApplicationEventPublisher;

import java.net.URI;
import java.util.Objects;

import static de.otto.edison.jobs.eventbus.events.MessageEvent.Level.*;
import static de.otto.edison.jobs.eventbus.events.MessageEvent.newMessageEvent;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.newStateChangeEvent;

@Immutable
public class JobEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final JobRunnable jobRunnable;
    private final URI jobUri;

    private JobEventPublisher(final ApplicationEventPublisher applicationEventPublisher,
                              final JobRunnable jobRunnable,
                              final URI jobUri) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.jobRunnable = jobRunnable;
        this.jobUri = jobUri;
    }

    public void stateChanged(final State state) {
        applicationEventPublisher.publishEvent(newStateChangeEvent(jobRunnable, jobUri, state));
    }

    public void message(final Level level, final String message) {
        applicationEventPublisher.publishEvent(newMessageEvent(jobRunnable, jobUri, level, message));
    }

    public void info(final String message) {
        message(INFO, message);
    }

    public void warn(final String message) {
        message(WARN, message);
    }

    public void error(final String message) {
        message(ERROR, message);
    }

    public static JobEventPublisher newJobEventPublisher(final ApplicationEventPublisher applicationEventPublisher,
                                                         final JobRunnable jobRunnable,
                                                         final URI jobUri) {
        return new JobEventPublisher(
                applicationEventPublisher,
                jobRunnable,
                jobUri
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobEventPublisher that = (JobEventPublisher) o;
        return Objects.equals(applicationEventPublisher, that.applicationEventPublisher) &&
                Objects.equals(jobRunnable, that.jobRunnable) &&
                Objects.equals(jobUri, that.jobUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationEventPublisher, jobRunnable, jobUri);
    }

    @Override
    public String toString() {
        return "JobEventPublisher{" +
                "applicationEventPublisher=" + applicationEventPublisher +
                ", jobRunnable=" + jobRunnable +
                ", jobUri=" + jobUri +
                '}';
    }
}
