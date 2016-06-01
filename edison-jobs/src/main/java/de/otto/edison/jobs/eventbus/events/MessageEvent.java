package de.otto.edison.jobs.eventbus.events;

import de.otto.edison.jobs.service.JobRunnable;
import net.jcip.annotations.Immutable;
import org.springframework.context.ApplicationEvent;

@Immutable
public class MessageEvent extends ApplicationEvent {

    private final String jobId;
    private final Level level;
    private final String message;

    private MessageEvent(final JobRunnable jobRunnable,
                        final String jobId,
                        final Level level,
                        final String message) {
        super(jobRunnable);
        this.jobId = jobId;
        this.level = level;
        this.message = message;
    }

    public String getJobId() {
        return jobId;
    }

    public String getMessage() {
        return message;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageEvent that = (MessageEvent) o;

        if (jobId != null ? !jobId.equals(that.jobId) : that.jobId != null) return false;
        if (level != that.level) return false;
        return !(message != null ? !message.equals(that.message) : that.message != null);

    }

    @Override
    public int hashCode() {
        int result = jobId != null ? jobId.hashCode() : 0;
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
                "jobId=" + jobId +
                ", level=" + level +
                ", message='" + message + '\'' +
                '}';
    }

    public static MessageEvent newMessageEvent(final JobRunnable jobRunnable, final String jobId, final Level level, final String message) {
        return new MessageEvent(jobRunnable, jobId, level, message);
    }

    public enum Level {
        INFO,
        WARN,
        ERROR;
    }
}
