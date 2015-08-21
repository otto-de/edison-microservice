package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.definition.JobDefinition;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

import static de.otto.edison.jobs.controller.JobDefinitionRepresentation.representationOf;
import static java.net.URI.create;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class JobDefinitionsControllerTest {

    @Test
    public void shouldReturn404IfJobDefinitionIsUnknown() throws IOException {
        // given
        final JobDefinitionsController controller = new JobDefinitionsController();

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("http://127.0.0.1/internal/jobdefinitions/FooJob");

        final HttpServletResponse response = mock(HttpServletResponse.class);
        // when
        controller.getJobDefinition("FooJob", request, response);
        // then
        verify(response).sendError(eq(404), anyString());
    }

    @Test
    public void shouldReturnJobDefinitionIfJobExists() throws IOException {
        // given
        final String jobType = "FooJob";
        final JobDefinition expectedDef = jobDefinition(jobType);

        final JobDefinitionsController controller = new JobDefinitionsController(asList(expectedDef));

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("http://127.0.0.1/internal/jobdefinitions/" + jobType);

        final HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        final JobDefinitionRepresentation jobDefinition = controller.getJobDefinition(jobType, request, response);

        // then
        assertThat(jobDefinition, is(representationOf(expectedDef, "")));
    }

    /*
    @Test
    public void shouldReturnAllJobs() throws IOException {
        // given
        final JobDefinition fooJobDef = jobDefinition("FooJob");
        final JobDefinition barJobDef = jobDefinition("BarJob");

        final JobDefinitionsController controller = new JobDefinitionsController(asList(fooJobDef, barJobDef));

        // when
        Object job = controller.getJobDefinitions(null, 100, mock(HttpServletRequest.class));

        // then
        assertThat(job, is(asList(representationOf(firstJob, ""), representationOf(secondJob, ""))));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnAllJobsOfTypeAsHtml() {
        final JobInfo firstJob = newJobInfo(create("/test/42"), "SOME_TYPE", (j) -> {}, systemDefaultZone());
        final JobService service = mock(JobService.class);
        when(service.findJobs(Optional.of("SOME_TYPE"), 100)).thenReturn(asList(firstJob));

        final JobsController jobsController = new JobsController(service);

        ModelAndView modelAndView = jobsController.getJobsAsHtml("SOME_TYPE", mock(HttpServletRequest.class));
        List<JobRepresentation> jobs = (List<JobRepresentation>) modelAndView.getModel().get("jobs");
        assertThat(jobs, is(asList(representationOf(firstJob, false, ""))));
    }*/

    private JobDefinition jobDefinition(final String jobType) {
        return new JobDefinition() {

            @Override
            public URI triggerUri() {
                return create("/internal/jobdefinitions/" + jobType);
            }

            @Override
            public String jobType() {
                return jobType;
            }

            @Override
            public String jobName() {
                return "Foo";
            }
        };
    }

}