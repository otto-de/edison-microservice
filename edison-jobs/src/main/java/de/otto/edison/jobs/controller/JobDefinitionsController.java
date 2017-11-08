package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.service.JobDefinitionService;
import de.otto.edison.jobs.service.JobMetaService;
import de.otto.edison.navigation.NavBar;
import de.otto.edison.status.domain.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import static de.otto.edison.navigation.NavBarItem.navBarItem;
import static de.otto.edison.status.domain.Link.link;
import static de.otto.edison.util.UrlHelper.baseUriOf;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@ConditionalOnProperty(prefix = "edison.jobs", name = "external-trigger", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WebEndpointProperties.class)
public class JobDefinitionsController {

    private final String jobDefinitionsUri;

    private final JobDefinitionService jobDefinitionService;
    private final JobMetaService jobMetaService;
    private final WebEndpointProperties webEndpointProperties;

    @Autowired
    public JobDefinitionsController(final JobDefinitionService definitionService,
                                    final JobMetaService jobMetaService,
                                    final NavBar rightNavBar,
                                    final WebEndpointProperties webEndpointProperties) {
        this.jobDefinitionService = definitionService;
        this.jobMetaService = jobMetaService;
        this.webEndpointProperties = webEndpointProperties;
        jobDefinitionsUri = String.format("%s/jobdefinitions", webEndpointProperties.getBasePath());
        rightNavBar.register(navBarItem(10, "Job Definitions", jobDefinitionsUri));
    }

    @RequestMapping(value = "${management.endpoints.web.base-path}/jobdefinitions", method = GET, produces = "application/json")
    @ResponseBody
    public Map<String, List<Link>> getJobDefinitionsAsJson(final HttpServletRequest request) {
        final String baseUri = baseUriOf(request);
        return singletonMap("links", new ArrayList<Link>() {{
            addAll(jobDefinitionService.getJobDefinitions()
                    .stream()
                    .map((def) -> link(
                            "http://github.com/otto-de/edison/link-relations/job/definition",
                            baseUri + jobDefinitionsUri + "/" + def.jobType(),
                            def.jobName()))
                    .collect(toList()));
            add(link("self", baseUriOf(request) + jobDefinitionsUri, "Self"));
        }});
    }

    @RequestMapping(value = "${management.endpoints.web.base-path}/jobdefinitions", method = GET, produces = "*/*")
    public ModelAndView getJobDefinitionsAsHtml(final HttpServletRequest request) {
        return new ModelAndView("jobdefinitions", new HashMap<String, Object>() {{
            put("baseUri", baseUriOf(request));
            put("jobdefinitions", jobDefinitionService.getJobDefinitions()
                    .stream()
                    .map((def) -> {
                        final JobMeta jobMeta = jobMetaService.getJobMeta(def.jobType());
                        return new HashMap<String, Object>() {{
                            put("isDisabled", jobMeta != null && jobMeta.isDisabled());
                            put("comment", jobMeta != null ? jobMeta.getDisabledComment() : "");
                            put("jobType", def.jobType());
                            put("name", def.jobName());
                            put("description", def.description());
                            put("maxAge", def.maxAge().isPresent() ? def.maxAge().get().toMinutes() + " Minutes" : "unlimited");
                            put("frequency", frequencyOf(def));
                            put("retry", retryOf(def));
                        }};
                    })
                    .collect(toList()));
        }});
    }

    @RequestMapping(value = "${management.endpoints.web.base-path}/jobdefinitions/{jobType}", method = GET, produces = "application/json")
    @ResponseBody
    public JobDefinitionRepresentation getJobDefinition(final @PathVariable String jobType,
                                                        final HttpServletRequest request,
                                                        final HttpServletResponse response) throws IOException {

        Optional<JobDefinition> jobDefinition = jobDefinitionService.getJobDefinition(jobType);
        if (jobDefinition.isPresent()) {
            return representationOf(jobDefinition.get(), baseUriOf(request), webEndpointProperties.getBasePath());
        } else {
            response.sendError(SC_NOT_FOUND, "Job not found");
            return null;
        }
    }

    @RequestMapping(value = "${management.endpoints.web.base-path}/jobdefinitions/{jobType}", method = GET, produces = "*/*")
    public ModelAndView getJobDefinitionAsHtml(final @PathVariable String jobType,
                                               final HttpServletRequest request,
                                               final HttpServletResponse response) throws IOException {
        final JobMeta jobMeta = jobMetaService.getJobMeta(jobType);
        final Optional<HashMap<String, Object>> optionalResult = jobDefinitionService.getJobDefinition(jobType)
                .map((def) -> new HashMap<String, Object>() {{
                    put("isDisabled", jobMeta.isDisabled());
                    put("comment", jobMeta.getDisabledComment());
                    put("jobType", def.jobType());
                    put("name", def.jobName());
                    put("description", def.description());
                    put("maxAge", def.maxAge().isPresent() ? def.maxAge().get().toMinutes() + " Minutes" : "unlimited");
                    put("frequency", frequencyOf(def));
                    put("retry", retryOf(def));
                }});
        if (optionalResult.isPresent()) {
            return new ModelAndView("jobdefinitions", new HashMap<String, Object>() {{
                put("baseUri", baseUriOf(request));
                put("jobdefinitions", singletonList(optionalResult.get()));
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
