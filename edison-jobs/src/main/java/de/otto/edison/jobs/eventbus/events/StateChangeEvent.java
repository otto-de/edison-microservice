package de.otto.edison.jobs.eventbus.events;

import de.otto.edison.jobs.service.JobRunnable;
import net.jcip.annotations.Immutable;
import org.springframework.context.ApplicationEvent;

import java.util.Objects;

@Immutable
public class StateChangeEvent extends ApplicationEvent {

    private final String jobId;
    private final String jobType;
    private final State state;
    private final String message;

    private StateChangeEvent(final JobRunnable jobRunnable,
                             final String jobId,
                             final State state,
                             final String message) {
        super(jobRunnable);
        this.jobId = jobId;
        this.jobType = jobRunnable.getJobDefinition().jobType();
        this.state = state;
        this.message = message;
    }

    public String getJobId() {
        return jobId;
    }

    public State getState() {
        return state;
    }

    public String getJobType() {
        return jobType;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateChangeEvent that = (StateChangeEvent) o;
        return Objects.equals(jobId, that.jobId) &&
                Objects.equals(jobType, that.jobType) &&
                state == that.state &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, jobType, state, message);
    }

    @Override
    public String toString() {
        return "StateChangeEvent{" +
                "jobId='" + jobId + '\'' +
                ", jobType='" + jobType + '\'' +
                ", state=" + state +
                ", message='" + message + '\'' +
                "} " + super.toString();
    }

    public static StateChangeEvent newStateChangeEvent(final JobRunnable jobRunnable,
                                                       final String jobId,
                                                       final State state) {
        return new StateChangeEvent(jobRunnable, jobId, state, "");
    }

    public static StateChangeEvent newStateChangeEvent(final JobRunnable jobRunnable,
                                                       final String jobId,
                                                       final State state,
                                                       final String message) {
        return new StateChangeEvent(jobRunnable, jobId, state, message);
    }

    public enum State {
        START,
        STOP,
        FAILED,
        SKIPPED,
        RESTART,
        KEEP_ALIVE,
        DEAD
    }
}
