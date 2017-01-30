package de.otto.edison.jobs.domain;

import java.util.Objects;

/**
 * Created by guido on 30.01.17.
 */
public class RunningJob {
    public final String jobId;
    public final String jobType;

    public RunningJob(String jobId, String jobType) {
        this.jobId = jobId;
        this.jobType = jobType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunningJob that = (RunningJob) o;
        return Objects.equals(jobId, that.jobId) &&
                Objects.equals(jobType, that.jobType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, jobType);
    }

    @Override
    public String toString() {
        return "RunningJob{" +
                "jobId='" + jobId + '\'' +
                ", jobType='" + jobType + '\'' +
                '}';
    }
}
