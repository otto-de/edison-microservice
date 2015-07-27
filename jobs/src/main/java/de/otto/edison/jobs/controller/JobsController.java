package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.otto.edison.jobs.controller.JobRepresentation.representationOf;
import static java.net.URI.create;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
    public ModelAndView getJobsAsHtml(@RequestParam(value = "type", required = false) String type) {
        final List<JobRepresentation> jobRepresentations = jobService.findJobs(Optional.ofNullable(type), JOB_VIEW_COUNT)
                .stream()
                .map((j) -> representationOf(j, true))
                .collect(toList());
        final ModelAndView modelAndView = new ModelAndView("jobs");
        modelAndView.addObject("jobs", jobRepresentations);
        return modelAndView;
    }

    @RequestMapping(value = "/internal/jobs", method = GET, produces = "application/json")
    public List<JobRepresentation> getJobsAsJson(@RequestParam(value = "type", required = false) String type,
                                                 @RequestParam(value = "count", defaultValue = "1") int count) {
        return jobService.findJobs(Optional.ofNullable(type), count)
                .stream()
                .map((j) -> representationOf(j, false))
                .collect(toList());
    }

    @RequestMapping(value = "/internal/jobs", method = DELETE)
    public void deleteJobs(@RequestParam(value = "type", required = false) String type) {
        jobService.deleteJobs(Optional.ofNullable(type));
    }

    @RequestMapping(
            value = "/internal/jobs/{jobType}",
            method = POST)
    public void startJob(final @PathVariable String jobType,
                         final HttpServletResponse response) throws IOException {
        final URI jobUri = jobService.startAsyncJob(jobType);
        response.setHeader("Location", jobUri.toString());
        response.setStatus(SC_NO_CONTENT);
    }


    @RequestMapping(value = "/internal/jobs/{id}", method = GET, produces = "text/html")
    public ModelAndView findJobAsHtml(final HttpServletRequest request,
                                      final HttpServletResponse response) throws IOException {
        final URI uri = create(request.getRequestURI());

        final Optional<JobInfo> optionalJob = jobService.findJob(uri);
        if (optionalJob.isPresent()) {
            final ModelAndView modelAndView = new ModelAndView("job");
            modelAndView.addObject("job", representationOf(optionalJob.get(), true));
            return modelAndView;
        } else {
            response.sendError(SC_NOT_FOUND, "Job not found");
            return null;
        }
    }

    @RequestMapping(value = "/internal/jobs/{id}", method = GET, produces = "application/json")
    public JobRepresentation findJob(final HttpServletRequest request,
                                     final HttpServletResponse response) throws IOException {

        final URI uri = create(request.getRequestURI());

        final Optional<JobInfo> optionalJob = jobService.findJob(uri);
        if (optionalJob.isPresent()) {
            return representationOf(optionalJob.get(), false);
        } else {
            response.sendError(SC_NOT_FOUND, "Job not found");
            return null;
        }
    }

}
