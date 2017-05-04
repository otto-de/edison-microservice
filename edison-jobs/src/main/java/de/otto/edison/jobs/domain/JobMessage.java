package de.otto.edison.jobs.domain;

import java.time.OffsetDateTime;

/**
 * @author Guido Steinacker
 * @since 23.02.15
 */
public final class JobMessage {

    private final Level level;
    private final String message;
    private final OffsetDateTime timestamp;

    private JobMessage(final Level level, final String message, final OffsetDateTime timestamp) {
        this.level = level;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static JobMessage jobMessage(final Level level, final String message, final OffsetDateTime ts) {
        return new JobMessage(level, message, ts);
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobMessage that = (JobMessage) o;

        if (level != that.level) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = level != null ? level.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JobMessage{" +
                "level=" + level +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
