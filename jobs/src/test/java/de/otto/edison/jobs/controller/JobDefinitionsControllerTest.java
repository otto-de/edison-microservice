package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.definition.DefaultJobDefinition;
import de.otto.edison.jobs.definition.JobDefinition;
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
import static java.time.Duration.ofHours;
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
        final DefaultJobDefinition expectedDef = jobDefinition(jobType, "Foo");

        final JobDefinitionsController controller = new JobDefinitionsController(asList(expectedDef));

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

        final JobDefinitionsController controller = new JobDefinitionsController(asList(fooJobDef, barJobDef));

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://127.0.0.1/internal/jobdefinitions/"));
        when(request.getServletPath()).thenReturn("/internal/jobdefinitions/");

        // when
        Map<String, List<Link>> defs = controller.getJobDefinitions(request);

        // then
        assertThat(defs.get("jobdefinitions"), is(
                asList(
                        link("jobdefinition", "http://127.0.0.1/internal/jobdefinitions/FooJob", "Foo"),
                        link("jobdefinition", "http://127.0.0.1/internal/jobdefinitions/BarJob", "Bar")))
        );
        assertThat(defs.get("links"), is(
                asList(
                        link("self", "http://127.0.0.1/internal/jobdefinitions/", "Self")))
        );
    }

    private DefaultJobDefinition jobDefinition(final String jobType, final String name) {
        return fixedDelayJobDefinition(jobType, name, name, ofHours(1), Optional.<Duration>empty());
    }

}