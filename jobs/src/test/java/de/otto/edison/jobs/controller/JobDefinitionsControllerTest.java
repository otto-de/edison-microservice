package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.definition.DefaultJobDefinition;
import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.service.JobDefinitionService;

import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.otto.edison.jobs.controller.JobDefinitionRepresentation.representationOf;
import static de.otto.edison.jobs.controller.Link.link;
import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static de.otto.edison.jobs.definition.DefaultJobDefinition.notTriggerableJobDefinition;
import static java.time.Duration.ofHours;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Test
public class JobDefinitionsControllerTest {

    @Test
    public void shouldReturn404IfJobDefinitionIsUnknown() throws IOException {
        // given
        final JobDefinitionsController controller = new JobDefinitionsController(new JobDefinitionService());

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1/internal/jobdefinitions/FooJob"));
        when(request.getServletPath()).thenReturn("/internal/jobdefinitions/FooJob");

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
        final JobDefinition expectedDef = jobDefinition(jobType, "Foo");

        final JobDefinitionService service = new JobDefinitionService(asList(expectedDef));
        final JobDefinitionsController controller = new JobDefinitionsController(service);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1/internal/jobdefinitions/" + jobType));
        when(request.getServletPath()).thenReturn("/internal/jobdefinitions/" + jobType);

        final HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        final JobDefinitionRepresentation jobDefinition = controller.getJobDefinition(jobType, request, response);

        // then
        assertThat(jobDefinition, is(representationOf(expectedDef, "http://127.0.0.1")));
    }

    @Test
    public void shouldReturnAllJobDefinitions() throws IOException {
        // given
        final JobDefinition fooJobDef = jobDefinition("FooJob", "Foo");
        final JobDefinition barJobDef = jobDefinition("BarJob", "Bar");

        final JobDefinitionService service = new JobDefinitionService(asList(fooJobDef, barJobDef));
        final JobDefinitionsController controller = new JobDefinitionsController(service);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1/internal/jobdefinitions/"));
        when(request.getServletPath()).thenReturn("/internal/jobdefinitions/");

        // when
        Map<String, List<Link>> defs = controller.getJobDefinitionsAsJson(request);

        // then
        assertThat(defs.get("links"), is(asList(
                        link("http://github.com/otto-de/edison/link-relations/job/definition", "http://127.0.0.1/internal/jobdefinitions/FooJob", "Foo"),
                        link("http://github.com/otto-de/edison/link-relations/job/definition", "http://127.0.0.1/internal/jobdefinitions/BarJob", "Bar"),
                        link("self", "http://127.0.0.1/internal/jobdefinitions", "Self")))
        );
    }

    @Test
    public void shouldReturnAllJobDefinitionsAsHtml() throws IOException {
        // given
        final JobDefinition fooJobDef = jobDefinition("FooJob", "Foo");
        final JobDefinition barJobDef = notTriggerableDefinition("BarJob", "Bar");

        final JobDefinitionService service = new JobDefinitionService(asList(fooJobDef, barJobDef));
        final JobDefinitionsController controller = new JobDefinitionsController(service);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1/internal/jobdefinitions/"));
        when(request.getServletPath()).thenReturn("/internal/jobdefinitions/");

        // when
        ModelAndView modelAndView = controller.getJobDefinitionsAsHtml(request);
        List<Object> jobdefinitions = (List<Object>) modelAndView.getModel().get("jobdefinitions");
 
        // then
        assertThat(jobdefinitions.size(), is(2));
        assertThat(jobdefinitions.get(0).toString(), containsString("frequency=Every 60 Minutes"));
        assertThat(jobdefinitions.get(1).toString(), containsString("frequency=Never"));
   }

    private JobDefinition jobDefinition(final String jobType, final String name) {
        return fixedDelayJobDefinition(jobType, name, name, ofHours(1), Optional.<Duration>empty());
    }

    private JobDefinition notTriggerableDefinition(final String jobType, final String name) {
        return notTriggerableJobDefinition(jobType, name, name);
    }

}