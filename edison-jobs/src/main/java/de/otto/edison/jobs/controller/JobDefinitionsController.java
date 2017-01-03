package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.service.JobDefinitionService;
import de.otto.edison.navigation.NavBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

import static de.otto.edison.jobs.controller.JobDefinitionRepresentation.representationOf;
import static de.otto.edison.jobs.controller.Link.link;
import static de.otto.edison.jobs.controller.UrlHelper.baseUriOf;
import static de.otto.edison.navigation.NavBarItem.navBarItem;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@ConditionalOnProperty(name = "edison.jobs.web.controller.enabled", havingValue = "true", matchIfMissing = true)
public class JobDefinitionsController {

    public static final String INTERNAL_JOBDEFINITIONS = "/internal/jobdefinitions";

    private final JobDefinitionService jobDefinitions;
    private final JobRepository jobRepository;

    @Autowired
    public JobDefinitionsController(final JobDefinitionService service,
                                    final JobRepository jobRepository,
                                    final NavBar rightNavBar) {
        this.jobDefinitions = service;
        this.jobRepository = jobRepository;
        rightNavBar.register(navBarItem(10, "Job Definitions", INTERNAL_JOBDEFINITIONS));
    }

    @RequestMapping(value = INTERNAL_JOBDEFINITIONS, method = GET, produces = "application/json")
    @ResponseBody
    public Map<String, List<Link>> getJobDefinitionsAsJson(final HttpServletRequest request) {
        final String baseUri = baseUriOf(request);
        return singletonMap("links", new ArrayList<Link>() {{
            addAll(jobDefinitions.getJobDefinitions()
                    .stream()
                    .map((def) -> link(
                            "http://github.com/otto-de/edison/link-relations/job/definition",
                            baseUri + INTERNAL_JOBDEFINITIONS + "/" + def.jobType(),
                            def.jobName()))
                    .collect(toList()));
            add(link("self", baseUriOf(request) + INTERNAL_JOBDEFINITIONS, "Self"));
        }});
    }

    @RequestMapping(value = INTERNAL_JOBDEFINITIONS, method = GET, produces = "*/*")
    public ModelAndView getJobDefinitionsAsHtml(final HttpServletRequest request) {
        return new ModelAndView("jobdefinitions", new HashMap<String, Object>() {{
            put("baseUri", baseUriOf(request));
            put("disabledJobs", jobRepository.findDisabledJobTypes());
            put("jobdefinitions", jobDefinitions.getJobDefinitions()
                    .stream()
                    .map((def) -> new HashMap<String, Object>() {{
                        put("jobType", def.jobType());
                        put("name", def.jobName());
                        put("description", def.description());
                        put("maxAge", def.maxAge().isPresent() ? def.maxAge().get().toMinutes() + " Minutes" : "unlimited");
                        put("frequency", frequencyOf(def));
                        put("retry", retryOf(def));
                    }})
                    .collect(toList()));
        }});
    }

    @RequestMapping(value = INTERNAL_JOBDEFINITIONS + "/{jobType}", method = GET, produces = "application/json")
    @ResponseBody
    public JobDefinitionRepresentation getJobDefinition(final @PathVariable String jobType,
                                                        final HttpServletRequest request,
                                                        final HttpServletResponse response) throws IOException {

        Optional<JobDefinition> jobDefinition = jobDefinitions.getJobDefinition(jobType);
        if (jobDefinition.isPresent()) {
            return representationOf(jobDefinition.get(), baseUriOf(request));
        } else {
            response.sendError(SC_NOT_FOUND, "Job not found");
            return null;
        }
    }

    @RequestMapping(value = INTERNAL_JOBDEFINITIONS + "/{jobType}", method = GET, produces = "*/*")
    public ModelAndView getJobDefinitionAsHtml(final @PathVariable String jobType,
                                               final HttpServletRequest request,
                                               final HttpServletResponse response) throws IOException {
        final Optional<HashMap<String, Object>> optionalResult = jobDefinitions.getJobDefinition(jobType)
                .map((def) -> new HashMap<String, Object>() {{
                    put("jobType", def.jobType());
                    put("name", def.jobName());
                    put("description", def.description());
                    put("maxAge", def.maxAge().isPresent() ? def.maxAge().get().toMinutes() + " Minutes" : "unlimited");
                    put("frequency", frequencyOf(def));
                    put("retry", retryOf(def));
                }});
        if (optionalResult.isPresent()) {
            return new ModelAndView("jobdefinition", new HashMap<String, Object>() {{
                put("baseUri", baseUriOf(request));
                put("def", optionalResult.get());
            }});
        } else {
            response.sendError(SC_NOT_FOUND, "JobDefinition " + jobType + " not found.");
            return null;
        }
    }

    private String frequencyOf(final JobDefinition def) {
        if (def.cron().isPresent()) {
            return def.cron().get();
        } else {
            return fixedDelayFrequency(def.fixedDelay());
        }
    }

    private String fixedDelayFrequency(Optional<Duration> duration) {
        if (duration.isPresent()) {
            if (duration.get().toMinutes() < 1) {
                return "Every " + duration.get().toMillis()/1000 + " Seconds";
            } else {
                return "Every " + duration.get().toMinutes() + " Minutes";
            }
        } else {
            return "Never";
        }
    }

    private String retryOf(final JobDefinition def) {
        final String delay = def.retryDelay().isPresent() ? " with " + def.retryDelay().get().getSeconds() + " seconds delay." : ".";
        return def.retries() == 0 ? "Do not retry triggering" : "Retry trigger " + def.retries() + " times" + delay;
    }
}
