package de.otto.edison.jobs.controller;

import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.service.JobMetaService;
import de.otto.edison.jobs.service.JobService;
import de.otto.edison.navigation.NavBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.controller.JobRepresentation.representationOf;
import static de.otto.edison.navigation.NavBarItem.navBarItem;
import static de.otto.edison.util.UrlHelper.baseUriOf;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Controller
@ConditionalOnProperty(prefix = "edison.jobs", name = "external-trigger", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(EdisonApplicationProperties.class)
public class JobsController {

    private static final Logger LOG = LoggerFactory.getLogger(JobsController.class);

    private final JobService jobService;
    private final JobMetaService jobMetaService;
    private final EdisonApplicationProperties applicationProperties;

    @Autowired
    JobsController(final JobService jobService,
                   final JobMetaService jobMetaService,
                   final NavBar rightNavBar,
                   final EdisonApplicationProperties applicationProperties) {
        this.jobService = jobService;
        this.jobMetaService = jobMetaService;
        this.applicationProperties = applicationProperties;
        rightNavBar.register(navBarItem(10, "Job Overview", applicationProperties.getManagement().getBasePath() + "/jobs"));
    }

    @RequestMapping(value = "${edison.application.management.base-path:/internal}/jobs", method = GET, produces = "text/html")
    public ModelAndView getJobsAsHtml(@RequestParam(value = "type", required = false) String type,
                                      @RequestParam(value = "count", defaultValue = "100") int count,
                                      @RequestParam(value = "distinct", defaultValue = "true", required = false) boolean distinct,
                                      HttpServletRequest request) {
        final List<JobRepresentation> jobRepresentations = getJobInfos(type, count, distinct).stream()
                .map((j) -> representationOf(j, getJobMeta(j.getJobType()), true, baseUriOf(request), applicationProperties.getManagement().getBasePath()))
                .collect(toList());

        final ModelAndView modelAndView = new ModelAndView("jobs");
        modelAndView.addObject("jobs", jobRepresentations);
        if (type != null) {
            modelAndView.addObject("typeFilter", type);
        }
        modelAndView.addObject("baseUri", baseUriOf(request));
        return modelAndView;
    }

    @RequestMapping(value = "${edison.application.management.base-path:/internal}/jobs", method = GET, produces = "application/json")
    @ResponseBody
    public List<JobRepresentation> getJobsAsJson(@RequestParam(name = "type", required = false) String type,
                                                 @RequestParam(name = "count", defaultValue = "10") int count,
                                                 @RequestParam(name = "distinct", defaultValue = "true", required = false) boolean distinct,
                                                 @RequestParam(name = "humanReadable", defaultValue = "false", required = false) boolean humanReadable,
                                                 HttpServletRequest request) {
        return getJobInfos(type, count, distinct)
                .stream()
                .map((j) -> representationOf(j, getJobMeta(j.getJobType()), humanReadable, baseUriOf(request), applicationProperties.getManagement().getBasePath()))
                .collect(toList());
    }

    @RequestMapping(value = "${edison.application.management.base-path:/internal}/jobs", method = DELETE)
    public void deleteJobs(@RequestParam(value = "type", required = false) String type) {
        jobService.deleteJobs(Optional.ofNullable(type));
    }

    /**
     * Starts a new job of the specified type, if no such job is currently running.
     * <p>
     * The method will return immediately, without waiting for the job to complete.
     * <p>
     * If a job with same type is running, the response will have HTTP status 409 CONFLICT,
     * otherwise HTTP 204 NO CONTENT is returned, together with the response header 'Location',
     * containing the full URL of the running job.
     *
     * @param jobType  the type of the started job
     * @param request  the HttpServletRequest used to determine the base URL
     * @param response the HttpServletResponse used to set the Location header
     * @throws IOException in case the job was not able to start properly (ie. conflict)
     */
    @RequestMapping(
            value = "${edison.application.management.base-path:/internal}/jobs/{jobType}",
            method = POST)
    public void startJob(final @PathVariable String jobType,
                         final HttpServletRequest request,
                         final HttpServletResponse response) throws IOException {
        final Optional<String> jobId = jobService.startAsyncJob(jobType);
        if (jobId.isPresent()) {
            response.setHeader("Location", String.format("%s%s/jobs/%s", baseUriOf(request), applicationProperties.getManagement().getBasePath(), jobId.get()));
            response.setStatus(SC_NO_CONTENT);
        } else {
            response.sendError(SC_CONFLICT);
        }
    }

    @RequestMapping(
            value = "${edison.application.management.base-path:/internal}/jobs/{jobType}/disable",
            method = POST
    )
    public String disableJobType(final @PathVariable String jobType,
                                 final @RequestParam(required = false) String disabledComment) {
        jobMetaService.disable(jobType, disabledComment);
        return String.format("redirect:%s/jobdefinitions", applicationProperties.getManagement().getBasePath());
    }

    @RequestMapping(
            value = "${edison.application.management.base-path:/internal}/jobs/{jobType}/enable",
            method = POST
    )
    public String enableJobType(final @PathVariable String jobType) {
        jobMetaService.enable(jobType);
        return "redirect:" + applicationProperties.getManagement().getBasePath() + "/jobdefinitions";
    }

    @RequestMapping(value = "${edison.application.management.base-path:/internal}/jobs/{id}", method = GET, produces = "text/html")
    public ModelAndView getJobAsHtml(final HttpServletRequest request,
                                     final HttpServletResponse response,
                                     @PathVariable("id") final String jobId) throws IOException {

        setCorsHeaders(response);

        final Optional<JobInfo> optionalJob = jobService.findJob(jobId);
        if (optionalJob.isPresent()) {
            final JobInfo jobInfo = optionalJob.get();
            final JobMeta jobMeta = getJobMeta(jobInfo.getJobType());
            final ModelAndView modelAndView = new ModelAndView("job");
            modelAndView
                    .addObject("job", representationOf(jobInfo, jobMeta, true, baseUriOf(request), applicationProperties.getManagement().getBasePath()))
                    .addObject("baseUri", baseUriOf(request));
            return modelAndView;
        } else {
            response.sendError(SC_NOT_FOUND, "Job not found");
            return null;
        }
    }

    @RequestMapping(value = "${edison.application.management.base-path:/internal}/jobs/{id}", method = GET, produces = "application/json")
    @ResponseBody
    public JobRepresentation getJob(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    @PathVariable("id") final String jobId,
                                    @RequestParam(name = "humanReadable", defaultValue = "false", required = false) boolean humanReadable
    ) throws IOException {

        setCorsHeaders(response);

        final Optional<JobInfo> optionalJob = jobService.findJob(jobId);
        if (optionalJob.isPresent()) {
            final JobInfo jobInfo = optionalJob.get();
            return representationOf(optionalJob.get(), getJobMeta(jobInfo.getJobType()), humanReadable, baseUriOf(request), applicationProperties.getManagement().getBasePath());
        } else {
            response.sendError(SC_NOT_FOUND, "Job not found");
            return null;
        }
    }

    private void setCorsHeaders(final HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Methods", "GET");
        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    private JobMeta getJobMeta(final String jobType) {
        if (jobMetaService != null) {
            return jobMetaService.getJobMeta(jobType);
        } else {
            return null;
        }
    }

    private List<JobInfo> getJobInfos(String type, int count, boolean distinct) {
        final List<JobInfo> jobInfos;
        if (type == null && distinct) {
            jobInfos = jobService.findJobsDistinct();
        } else {
            jobInfos = jobService.findJobs(Optional.ofNullable(type), count);
        }
        return jobInfos;
    }
}
