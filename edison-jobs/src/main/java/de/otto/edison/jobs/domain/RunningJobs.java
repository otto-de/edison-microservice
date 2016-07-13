package de.otto.edison.jobs.domain;

import java.util.List;
import java.util.Objects;

public class RunningJobs {
    private final List<RunningJob> runningJobs;

    public List<RunningJob> getRunningJobs() {
        return runningJobs;
    }

    public RunningJobs(List<RunningJob> runningJobs) {
        this.runningJobs = runningJobs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunningJobs that = (RunningJobs) o;
        return Objects.equals(runningJobs, that.runningJobs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runningJobs);
    }

    @Override
    public String toString() {
        return "RunningJobs{" +
                "runningJobs=" + runningJobs +
                '}';
    }


    public static class RunningJob {
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
}
