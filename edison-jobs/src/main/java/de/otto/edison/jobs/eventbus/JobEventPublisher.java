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
    private final String jobId;

    private JobEventPublisher(final ApplicationEventPublisher applicationEventPublisher,
                              final JobRunnable jobRunnable,
                              final String jobId) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.jobRunnable = jobRunnable;
        this.jobId = jobId;
    }

    public void stateChanged(final State state) {
        applicationEventPublisher.publishEvent(newStateChangeEvent(jobRunnable, jobId, state));
    }

    public void message(final Level level, final String message) {
        applicationEventPublisher.publishEvent(newMessageEvent(jobRunnable, jobId, level, message));
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
                                                         final String jobId) {
        return new JobEventPublisher(
                applicationEventPublisher,
                jobRunnable,
                jobId
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobEventPublisher that = (JobEventPublisher) o;
        return Objects.equals(applicationEventPublisher, that.applicationEventPublisher) &&
                Objects.equals(jobRunnable, that.jobRunnable) &&
                Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicationEventPublisher, jobRunnable, jobId);
    }

    @Override
    public String toString() {
        return "JobEventPublisher{" +
                "applicationEventPublisher=" + applicationEventPublisher +
                ", jobRunnable=" + jobRunnable +
                ", jobId=" + jobId +
                '}';
    }
}
