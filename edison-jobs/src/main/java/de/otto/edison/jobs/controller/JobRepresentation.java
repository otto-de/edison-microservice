package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.domain.DisabledJob;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.status.domain.Link;

import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.status.domain.Link.link;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.DateTimeFormatter.ofLocalizedTime;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;
import static java.util.Arrays.asList;

public class JobRepresentation {

    private final JobInfo job;
    private final String baseUri;
    private final boolean humanReadable;
    private final DisabledJob disabledJobInfo;

    private JobRepresentation(final JobInfo job,
                              final DisabledJob disabledJobInfo,
                              final boolean humanReadable,
                              final String baseUri) {
        this.job = job;
        this.humanReadable=humanReadable;
        this.baseUri = baseUri;
        this.disabledJobInfo = disabledJobInfo;
    }

    public static JobRepresentation representationOf(final JobInfo job,
                                                     final DisabledJob disabledJobInfo,
                                                     final boolean humanReadable, final String baseUri) {
        return new JobRepresentation(job, disabledJobInfo, humanReadable, baseUri);
    }

    public String getJobUri() {
        return baseUri + "/internal/jobs/" + job.getJobId();
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

    public String getRuntime() {
        return job.isStopped()
                ? formatRuntime(job.getStarted(), job.getStopped().get())
                : "";
    }

	public String getLastUpdated() {
        return formatTime(job.getLastUpdated());
    }

    public String getHostname() {
        return job.getHostname();
    }

    public boolean getIsDisabled() {
        return disabledJobInfo != null;
    }

    public String getComment() {
        return disabledJobInfo == null ? "" : disabledJobInfo.comment;
    }

    public List<String> getMessages() {
        return job.getMessages().stream().map((jobMessage) ->
            "[" + formatTime(jobMessage.getTimestamp()) + "] [" + jobMessage.getLevel().getKey() + "] " + jobMessage.getMessage()
        ).collect(Collectors.toList());
    }

    public List<Link> getLinks() {
        final String jobUri = baseUri + "/internal/jobs/" + job.getJobId();
        return asList(
                link("self", jobUri, "Self"),
                link("http://github.com/otto-de/edison/link-relations/job/definition", baseUri + "/internal/jobdefinitions/" + job.getJobType(), "Job Definition"),
                link("collection", jobUri.substring(0, jobUri.lastIndexOf("/")), "All Jobs"),
                link("collection/" + getJobType(), jobUri.substring(0, jobUri.lastIndexOf("/")) + "?type=" + getJobType(), "All " + getJobType() + " Jobs")
        );
    }

    private String formatRuntime(OffsetDateTime started, OffsetDateTime stopped) {
        Duration duration = Duration.between(started, stopped);

        if (duration.toHours() >= 24) {
            return "> 24h";
        }

        LocalTime dateTime = LocalTime.ofSecondOfDay(duration.getSeconds());
        return humanReadable
                ? ofPattern("HH:mm:ss").format(dateTime)
                : ofPattern("HH:mm:ss").format(dateTime);
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
