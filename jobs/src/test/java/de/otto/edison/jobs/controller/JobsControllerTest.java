package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.service.JobService;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.controller.JobRepresentation.representationOf;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.net.URI.create;
import static java.time.Clock.fixed;
import static java.time.Clock.systemDefaultZone;
import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.systemDefault;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class JobsControllerTest {

    @Test
    public void shouldReturn404IfJobIsUnknown() throws IOException {
        // given
        final JobService jobService = mock(JobService.class);
        when(jobService.findJob(any(URI.class))).thenReturn(Optional.<JobInfo>empty());

        final JobsController jobsController = new JobsController(jobService);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("http://127.0.0.1/jobs/42");

        final HttpServletResponse response = mock(HttpServletResponse.class);
        // when
        jobsController.findJob(request, response);
        // then
        verify(response).sendError(eq(404), anyString());
    }

    @Test
    public void shouldReturnJobIfJobExists() throws IOException {
        // given
        final JobInfo expectedJob = newJobInfo("TEST", create("/test/42"), (j) -> {}, systemDefaultZone());
        final JobService jobService = mock(JobService.class);
        when(jobService.findJob(any(URI.class))).thenReturn(Optional.of(expectedJob));

        final JobsController jobsController = new JobsController(jobService);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(expectedJob.getJobUri().toString());

        final HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        final JobRepresentation job = jobsController.findJob(request, response);

        // then
        assertThat(job, is(representationOf(expectedJob)));
    }

    @Test
    public void shouldReturnAllJobs() throws IOException {
        // given
        final JobInfo firstJob = newJobInfo("TEST", create("/test/42"), (j) -> {}, fixed(ofEpochMilli(0), systemDefault()));
        final JobInfo secondJob = newJobInfo("TEST", create("/test/42"), (j) -> {}, fixed(ofEpochMilli(1), systemDefault()));
        final JobService service = mock(JobService.class);
        when(service.findJobs(Optional.<String>empty(), 100)).thenReturn(asList(firstJob, secondJob));

        final JobsController jobsController = new JobsController(service);

        // when
        Object job = jobsController.getJobsAsJson(null, 100);

        // then
        assertThat(job, is(asList(representationOf(firstJob), representationOf(secondJob))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnAllJobsOfTypeAsHtml() {
        final JobInfo firstJob = newJobInfo("SOME_TYPE", create("/test/42"), (j) -> {}, systemDefaultZone());
        final JobService service = mock(JobService.class);
        when(service.findJobs(Optional.of("SOME_TYPE"), 100)).thenReturn(asList(firstJob));

        final JobsController jobsController = new JobsController(service);

        ModelAndView modelAndView = jobsController.getJobsAsHtml("SOME_TYPE");
        List<JobRepresentation> jobs = (List<JobRepresentation>) modelAndView.getModel().get("jobs");
        assertThat(jobs, is(asList(representationOf(firstJob))));
    }

}