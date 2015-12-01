package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.domain.JobInfo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.jobs.controller.Link.link;
import static java.time.format.DateTimeFormatter.*;
import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;
import static java.util.Arrays.asList;

public class JobRepresentation {

    private final JobInfo job;
    private final boolean humanReadable;
    private final String baseUri;

    private JobRepresentation(final JobInfo job, final boolean humanReadable, final String baseUri) {
        this.job = job;
        this.humanReadable=humanReadable;
        this.baseUri = baseUri;
    }

    public static JobRepresentation representationOf(final JobInfo job, final boolean humanReadable, final String baseUri) {
        return new JobRepresentation(job, humanReadable, baseUri);
    }

    public String getJobUri() {
        return baseUri + job.getJobUri().toString();
    }

    public String getJobType() {
        return job.getJobType();
    }

    public String getStatus() {
        return job.getStatus().name();
    }

    public String getState() {
        return job.isStopped() ? "Stopped" : "Running";
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

    public List<Link> getLinks() {
        final String jobUri = baseUri + job.getJobUri().toString();
        return asList(
                link("self", jobUri, "Self"),
                link("http://github.com/otto-de/edison/link-relations/job/definition", baseUri + "/internal/jobdefinitions/" + job.getJobType(), "Job Definition"),
                link("collection", jobUri.substring(0, jobUri.lastIndexOf("/")), "All Jobs"),
                link("collection/" + getJobType(), jobUri.substring(0, jobUri.lastIndexOf("/")) + "?type=" + getJobType(), "All " + getJobType() + " Jobs")
        );
    }

    private String formatDateTime(final OffsetDateTime dateTime) {
        if (dateTime==null) {
            return null;
        } else {
            return humanReadable
                    ? ofLocalizedDateTime(SHORT, MEDIUM).format(dateTime)
                    : ISO_OFFSET_DATE_TIME.format(dateTime);
        }
    }

    private String formatTime(final OffsetDateTime dateTime) {
        if (dateTime==null) {
            return null;
        } else {
            return humanReadable
                    ? ofLocalizedTime(MEDIUM).format(dateTime)
                    : ISO_OFFSET_DATE_TIME.format(dateTime);
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
