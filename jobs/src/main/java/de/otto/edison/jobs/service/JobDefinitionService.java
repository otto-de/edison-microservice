package de.otto.edison.jobs.service;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import de.otto.edison.jobs.definition.JobDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;

/**
 * A service that is providing access to the configured JobDefinitions.
 *
 *
 * @author Guido Steinacker
 * @since 15.09.15
 */
@Service
public class JobDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(JobDefinitionService.class);

    @Autowired(required = false)
    private List<JobDefinition> jobDefinitions = new ArrayList<>();

    public JobDefinitionService() {
    }

    public JobDefinitionService(final List<JobDefinition> jobDefinitions) {
        this.jobDefinitions = jobDefinitions;
    }

    @PostConstruct
    public void postConstruct() {
        LOG.info("Initializing JobDefinitionService...");
        if (jobDefinitions == null) {
            jobDefinitions = emptyList();
        }
        if (jobDefinitions.size() == 0) {
            LOG.info("No JobDefinitions found in microservice.");
            return;
        } else {
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
