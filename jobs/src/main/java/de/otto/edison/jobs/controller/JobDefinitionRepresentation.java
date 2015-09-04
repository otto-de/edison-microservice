package de.otto.edison.jobs.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.jobs.definition.JobDefinition;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.controller.Link.link;
import static java.util.Arrays.asList;

/**
 * @author Guido Steinacker
 * @since 21.08.15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobDefinitionRepresentation {

    public final String type;
    public final String name;
    public final int retries;
    public final Long  retryDelay;
    public final String cron;
    public final Long maxAge;
    public final Long fixedDelay;
    public final List<Link> links;

    private JobDefinitionRepresentation(final JobDefinition jobDefinition, String baseUri) {
        this.type = jobDefinition.jobType();
        this.name = jobDefinition.jobName();
        this.retries = jobDefinition.retries();
        this.retryDelay = valueOf(jobDefinition.retryDelay());
        this.cron = jobDefinition.cron().orElse(null);
        this.maxAge = valueOf(jobDefinition.maxAge());
        this.fixedDelay = valueOf(jobDefinition.fixedDelay());
        this.links = linksOf(jobDefinition, baseUri);
    }

    public static JobDefinitionRepresentation representationOf(final JobDefinition jobDefinition, final String baseUri) {
        return new JobDefinitionRepresentation(jobDefinition, baseUri);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobDefinitionRepresentation that = (JobDefinitionRepresentation) o;

        if (retries != that.retries) return false;
        if (cron != null ? !cron.equals(that.cron) : that.cron != null) return false;
        if (fixedDelay != null ? !fixedDelay.equals(that.fixedDelay) : that.fixedDelay != null) return false;
        if (links != null ? !links.equals(that.links) : that.links != null) return false;
        if (maxAge != null ? !maxAge.equals(that.maxAge) : that.maxAge != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (retryDelay != null ? !retryDelay.equals(that.retryDelay) : that.retryDelay != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + retries;
        result = 31 * result + (retryDelay != null ? retryDelay.hashCode() : 0);
        result = 31 * result + (cron != null ? cron.hashCode() : 0);
        result = 31 * result + (maxAge != null ? maxAge.hashCode() : 0);
        result = 31 * result + (fixedDelay != null ? fixedDelay.hashCode() : 0);
        result = 31 * result + (links != null ? links.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JobDefinitionRepresentation{" +
                "cron='" + cron + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", retries=" + retries +
                ", retryDelay=" + retryDelay +
                ", maxAge=" + maxAge +
                ", fixedDelay=" + fixedDelay +
                ", links=" + links +
                '}';
    }

    private List<Link> linksOf(final JobDefinition jobDefinition, String baseUri) {
        return asList(
                link("self", baseUri + jobDefinition.triggerUrl().toString(), null),
                link("collection", baseUri + "/internal/jobdefinitions", null),
                link("trigger", baseUri + jobDefinition.triggerUrl().toString(), null),
                link("jobs", baseUri + "/internal/jobs?type=" + jobDefinition.jobType(), null)
        );
    }

    private Long valueOf(final Optional<Duration> duration) {
        if (duration.isPresent()) {
            return duration.get().getSeconds();
        } else {
            return null;
        }
    }
}
