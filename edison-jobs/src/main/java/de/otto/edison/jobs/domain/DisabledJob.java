package de.otto.edison.jobs.domain;

import java.util.Objects;

/**
 * Information about a disabled Job.
 *
 * @since 1.0.0
 */
public class DisabledJob {
    public final String jobType;
    public final String comment;

    public DisabledJob(final String jobType, final String comment) {
        this.jobType = jobType;
        this.comment = comment != null ? comment : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DisabledJob that = (DisabledJob) o;
        return Objects.equals(jobType, that.jobType) &&
                Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobType, comment);
    }

    @Override
    public String toString() {
        return "DisabledJob{" +
                "jobType='" + jobType + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
