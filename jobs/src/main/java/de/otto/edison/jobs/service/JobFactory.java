package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.net.URI.create;
import static java.util.UUID.randomUUID;

@Component
public class JobFactory {

    @Value("${server.contextPath}")
    private String serverContextPath;

    public JobFactory() {
    }

    public JobFactory(final String serverContextPath) {
        this.serverContextPath = serverContextPath;
    }

    public JobInfo createJob(final JobType type) {
        return new JobInfo(type, create(serverContextPath + "/jobs/" + randomUUID()));
    }

}
