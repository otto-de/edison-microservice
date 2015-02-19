package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.domain.JobInfo;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class JobRepresentation {

    private final JobInfo job;

    private JobRepresentation(final JobInfo job) {
        this.job = job;
    }

    public static JobRepresentation representationOf(final JobInfo job) {
        return new JobRepresentation(job);
    }

    public String getJobUri() {
        return job.getJobUri().toString();
    }

    public String getJobType() {
        return job.getJobType().name();
    }

    public JobInfo.ExecutionState getState() {
        return job.getState();
    }

    public JobInfo.JobStatus getStatus() {
        return job.getStatus();
    }

    public String getStarted() {
        return ISO_LOCAL_DATE_TIME.format(job.getStarted());
    }

    public String getStopped() {
        return job.getStopped().isPresent()
                ? ISO_LOCAL_DATE_TIME.format(job.getStopped().get())
                : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobRepresentation that = (JobRepresentation) o;

        if (job != null ? !job.equals(that.job) : that.job != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return job != null ? job.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "JobRepresentation{" +
                "job=" + job +
                '}';
    }
}
