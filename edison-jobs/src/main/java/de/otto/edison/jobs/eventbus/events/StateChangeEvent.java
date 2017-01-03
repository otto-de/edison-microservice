package de.otto.edison.jobs.eventbus.events;

import de.otto.edison.jobs.service.JobRunnable;
import net.jcip.annotations.Immutable;
import org.springframework.context.ApplicationEvent;

@Immutable
public class StateChangeEvent extends ApplicationEvent {

    private final String jobId;
    private final String jobType;
    private final State state;

    private StateChangeEvent(final JobRunnable jobRunnable,
                             final String jobId,
                             final State state) {
        super(jobRunnable);
        this.jobId = jobId;
        this.jobType = jobRunnable.getJobDefinition().jobType();
        this.state = state;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateChangeEvent that = (StateChangeEvent) o;

        if (jobId != null ? !jobId.equals(that.jobId) : that.jobId != null) return false;
        if (jobType != null ? !jobType.equals(that.jobType) : that.jobType != null) return false;
        return state == that.state;

    }

    @Override
    public int hashCode() {
        int result = jobId != null ? jobId.hashCode() : 0;
        result = 31 * result + (jobType != null ? jobType.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StateChangeEvent{" +
                "jobId=" + jobId +
                ", jobType='" + jobType + '\'' +
                ", state=" + state +
                '}';
    }

    public static StateChangeEvent newStateChangeEvent(final JobRunnable jobRunnable,
                                                       final String jobId,
                                                       final State state) {
        return new StateChangeEvent(jobRunnable, jobId, state);
    }

    public enum State {
        START,
        STOP,
        RESTART,
        KEEP_ALIVE,
        DEAD
    }
}
