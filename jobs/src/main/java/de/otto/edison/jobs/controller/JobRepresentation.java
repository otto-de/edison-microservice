package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.domain.JobInfo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.DateTimeFormatter.ofLocalizedTime;
import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;

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
        return job.getJobType();
    }

    public String getStatus() {
        return job.getStatus().name();
    }

    public String getState() {
        return job.getState();
    }

    public String getStarted() {
        OffsetDateTime started = job.getStarted();
        return formatDateTime(started);
    }

    public String getStopped() {
        return job.isStopped()
                ? formatTime(job.getStopped().get())
                : "";
    }

    public String getLastUpdated() {
        return formatTime(job.getLastUpdated());
    }

    public List<String> getMessages() {
        return job.getMessages().stream().map((jobMessage) ->
            "[" + formatTime(jobMessage.getTimestamp()) + "] [" + jobMessage.getLevel().getKey() + "] " + jobMessage.getMessage()
        ).collect(Collectors.toList());
    }

    private String formatDateTime(OffsetDateTime dateTime) {
        if (dateTime==null) {
            return null;
        } else {
            return ofLocalizedDateTime(SHORT, MEDIUM).format(dateTime);
        }
    }

    private String formatTime(OffsetDateTime dateTime) {
        if (dateTime==null) {
            return null;
        } else {
            return ofLocalizedTime(MEDIUM).format(dateTime);
        }
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
