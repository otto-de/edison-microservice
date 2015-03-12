package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.InMemJobRepository;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import static de.otto.edison.jobs.controller.JobRepresentation.representationOf;
import static de.otto.edison.jobs.domain.JobInfoBuilder.copyOf;
import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static java.net.URI.create;
import static java.time.ZonedDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class JobsControllerTest {

    @Test
    public void shouldReturn404IfJobIsUnknown() throws IOException {
        final JobsController jobsController = new JobsController(new InMemJobRepository());

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("http://127.0.0.1/jobs/42");

        final HttpServletResponse response = mock(HttpServletResponse.class);
        jobsController.findJob(request, response);
        verify(response).sendError(eq(404), anyString());
    }

    @Test
    public void shouldReturnJobIfJobExists() throws IOException {
        final InMemJobRepository repository = new InMemJobRepository();
        final JobInfo expectedJob = jobInfoBuilder(() -> "TEST", create("/test/42")).build();
        repository.createOrUpdate(expectedJob);

        final JobsController jobsController = new JobsController(repository);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(expectedJob.getJobUri().toString());

        final HttpServletResponse response = mock(HttpServletResponse.class);
        Object job = jobsController.findJob(request, response);
        assertThat(job, is(representationOf(expectedJob)));
    }

    @Test
    public void shouldReturnAllJobs() throws IOException {
        final InMemJobRepository repository = new InMemJobRepository();
        final JobInfo firstJob = jobInfoBuilder(() -> "TEST", create("/test/42"))
                .build();
        final JobInfo secondJob = jobInfoBuilder(() -> "TEST", create("/test/43"))
                .withStarted(now().plus(10, MILLIS))
                .build();
        repository.createOrUpdate(firstJob);
        repository.createOrUpdate(secondJob);

        final JobsController jobsController = new JobsController(repository);

        Object job = jobsController.findJobsAsJson(null);
        assertThat(job, is(asList(representationOf(secondJob), representationOf(firstJob))));
    }

    @Test
    public void shouldReturnAllJobsOfType() {
        final InMemJobRepository repository = new InMemJobRepository();
        final JobInfo firstJob = jobInfoBuilder(() -> "SOME_TYPE", create("/test/42"))
                .build();
        final JobInfo secondJob = jobInfoBuilder(() -> "SOME_OTHER_TYPE", create("/test/43"))
                .build();
        repository.createOrUpdate(firstJob);
        repository.createOrUpdate(secondJob);

        final JobsController jobsController = new JobsController(repository);

        Object job = jobsController.findJobsAsJson("SOME_TYPE");
        assertThat(job, is(asList(representationOf(firstJob))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnAllJobsOfTypeAsHtml() {
        final InMemJobRepository repository = new InMemJobRepository();
        final JobInfo firstJob = jobInfoBuilder(() -> "SOME_TYPE", create("/test/42"))
                .build();
        final JobInfo secondJob = jobInfoBuilder(() -> "SOME_OTHER_TYPE", create("/test/43"))
                .build();
        repository.createOrUpdate(firstJob);
        repository.createOrUpdate(secondJob);

        final JobsController jobsController = new JobsController(repository);

        ModelAndView modelAndView = jobsController.findJobsAsHtml("SOME_TYPE");
        List<JobRepresentation> jobs = (List<JobRepresentation>) modelAndView.getModel().get("jobs");
        assertThat(jobs, is(asList(representationOf(firstJob))));
    }

}