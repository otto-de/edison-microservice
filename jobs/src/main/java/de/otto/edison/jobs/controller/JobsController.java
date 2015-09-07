package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.controller.JobRepresentation.representationOf;
import static de.otto.edison.jobs.controller.UrlHelper.baseUriOf;
import static java.net.URI.create;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class JobsController {

    private static final Logger LOG = LoggerFactory.getLogger(JobsController.class);
    public static final int JOB_VIEW_COUNT = 100;

    @Autowired
    private JobService jobService;
    @Value("${server.contextPath}")
    private String serverContextPath;


    public JobsController() {
    }

    JobsController(final JobService jobService) {
        this.jobService = jobService;
    }

    @RequestMapping(value = "/internal/jobs", method = GET, produces = "text/html")
    public ModelAndView getJobsAsHtml(@RequestParam(value = "type", required = false) String type,
                                      HttpServletRequest request) {
        final List<JobRepresentation> jobRepresentations = jobService.findJobs(Optional.ofNullable(type), JOB_VIEW_COUNT)
                .stream()
                .map((j) -> representationOf(j, true, baseUriOf(request)))
                .collect(toList());
        final ModelAndView modelAndView = new ModelAndView("jobs");
        modelAndView.addObject("jobs", jobRepresentations);
        return modelAndView;
    }

    @RequestMapping(value = "/internal/jobs", method = GET, produces = "application/json")
    public List<JobRepresentation> getJobsAsJson(@RequestParam(value = "type", required = false) String type,
                                                 @RequestParam(value = "count", defaultValue = "1") int count,
                                                 HttpServletRequest request) {
        return jobService.findJobs(Optional.ofNullable(type), count)
                .stream()
                .map((j) -> representationOf(j, false, baseUriOf(request)))
                .collect(toList());
    }

    @RequestMapping(value = "/internal/jobs", method = DELETE)
    public void deleteJobs(@RequestParam(value = "type", required = false) String type) {
        jobService.deleteJobs(Optional.ofNullable(type));
    }

    /**
     * Starts a new job of the specifed type, if no such job is currently running.
     *
     * The method will return immediately, without waiting for the job to complete.
     *
     * If a job with same type is running, the response will have HTTP status 409 CONFLICT,
     * otherwise HTTP 204 NO CONTENT is returned, together with the response header 'Location',
     * containing the full URL of the running job.
     *
     * @param jobType the type of the job
     * @throws IOException
     */
    @RequestMapping(
            value = "/internal/jobs/{jobType}",
            method = POST)
    public void startJob(final @PathVariable String jobType,
                         final HttpServletRequest request,
                         final HttpServletResponse response) throws IOException {
        final Optional<URI> jobUri = jobService.startAsyncJob(jobType);
        if (jobUri.isPresent()) {
            response.setHeader("Location", baseUriOf(request) + jobUri.get().toString());
            response.setStatus(SC_NO_CONTENT);
        } else {
            response.sendError(SC_CONFLICT);
        }
    }


    @RequestMapping(value = "/internal/jobs/{id}", method = GET, produces = "text/html")
    public ModelAndView findJobAsHtml(final HttpServletRequest request,
                                      final HttpServletResponse response) throws IOException {
        final URI uri = jobUriOf(request);

        final Optional<JobInfo> optionalJob = jobService.findJob(uri);
        if (optionalJob.isPresent()) {
            final ModelAndView modelAndView = new ModelAndView("job");
            modelAndView.addObject("job", representationOf(optionalJob.get(), true, baseUriOf(request)));
            return modelAndView;
        } else {
            response.sendError(SC_NOT_FOUND, "Job not found");
            return null;
        }
    }

    @RequestMapping(value = "/internal/jobs/{id}", method = GET, produces = "application/json")
    public JobRepresentation findJob(final HttpServletRequest request,
                                     final HttpServletResponse response) throws IOException {

        final URI uri = jobUriOf(request);

        final Optional<JobInfo> optionalJob = jobService.findJob(uri);
        if (optionalJob.isPresent()) {
            return representationOf(optionalJob.get(), false, baseUriOf(request));
        } else {
            response.sendError(SC_NOT_FOUND, "Job not found");
            return null;
        }
    }

    private void setCorsHeaders(final HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Methods", "GET");
        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    private URI jobUriOf(HttpServletRequest request) {
        String servletPath = request.getServletPath() != null ? request.getServletPath() : "";
        if (servletPath.contains(".")) {
            return create(servletPath.substring(0, servletPath.lastIndexOf('.')));
        } else {
            return create(servletPath);
        }
    }

}
