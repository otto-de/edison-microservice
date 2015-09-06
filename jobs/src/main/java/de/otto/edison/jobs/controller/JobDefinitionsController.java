package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.definition.JobDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static de.otto.edison.jobs.controller.JobDefinitionRepresentation.representationOf;
import static de.otto.edison.jobs.controller.Link.link;
import static de.otto.edison.jobs.controller.UrlHelper.baseUriOf;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class JobDefinitionsController {

    private static final Logger LOG = LoggerFactory.getLogger(JobDefinitionsController.class);
    public static final String INTERNAL_JOBDEFINITIONS = "/internal/jobdefinitions";

    @Autowired(required = false)
    private List<JobDefinition> jobDefinitions = Collections.emptyList();

    public JobDefinitionsController() {
    }

    public JobDefinitionsController(final List<JobDefinition> jobDefinitions) {
        this.jobDefinitions = jobDefinitions;
    }

    @RequestMapping(value = INTERNAL_JOBDEFINITIONS, method = GET, produces = "application/json")
    public Map<String, List<Link>> getJobDefinitions(final HttpServletRequest request) {
        final String baseUri = baseUriOf(request);
        return singletonMap("links", new ArrayList<Link>() {{
                addAll(jobDefinitions
                        .stream()
                        .map((def) -> link(
                                "http://github.com/otto-de/edison/link-relations/job/definition",
                                baseUri + INTERNAL_JOBDEFINITIONS + "/" + def.jobType(),
                                def.jobName()))
                        .collect(toList()));
                add(link("self", baseUriOf(request) + INTERNAL_JOBDEFINITIONS, "Self"));
        }});
    }

    @RequestMapping(value = INTERNAL_JOBDEFINITIONS + "/{jobType}", method = GET, produces = "application/json")
    public JobDefinitionRepresentation getJobDefinition(final @PathVariable String jobType,
                                                        final HttpServletRequest request,
                                                        final HttpServletResponse response) throws IOException {

        Optional<JobDefinition> jobDefinition = jobDefinitions.stream().filter((j) -> j.jobType().equals(jobType)).findAny();
        if (jobDefinition.isPresent()) {
            return representationOf(jobDefinition.get(), baseUriOf(request));
        } else {
            response.sendError(SC_NOT_FOUND, "Job not found");
            return null;
        }
    }
}
