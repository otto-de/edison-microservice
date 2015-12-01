package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * A service that is providing access to the configured JobDefinitions.
 *
 * @author Guido Steinacker
 * @since 15.09.15
 */
@Service
public class JobDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(JobDefinitionService.class);


    @Autowired(required = false)
    private List<JobRunnable> jobRunnables = new ArrayList<>();
    private List<JobDefinition> jobDefinitions = new ArrayList<>();

    // Used by Spring
    public JobDefinitionService() {
    }

    // Used by tests
    public JobDefinitionService(final List<JobRunnable> jobRunnables) {
        this.jobRunnables = jobRunnables;
        postConstruct();
    }

    @PostConstruct
    void postConstruct() {
        LOG.info("Initializing JobDefinitionService...");
        if (jobRunnables == null || jobRunnables.isEmpty()) {
            jobDefinitions = emptyList();
            LOG.info("No JobDefinitions found in microservice.");
        } else {
            this.jobDefinitions = jobRunnables.stream().map(JobRunnable::getJobDefinition).collect(toList());
            LOG.info("Found " + jobDefinitions.size() + " JobDefinitions: " + jobDefinitions.stream().map(JobDefinition::jobType).collect(toList()));
        }
    }

    public List<JobDefinition> getJobDefinitions() {
        return new ArrayList<>(jobDefinitions);
    }

    public Optional<JobDefinition> getJobDefinition(final String jobType) {
        return jobDefinitions
                .stream()
                .filter((j) -> j.jobType().equals(jobType))
                .findAny();
    }

}
