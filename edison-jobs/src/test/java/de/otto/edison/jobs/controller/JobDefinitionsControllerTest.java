package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.service.JobDefinitionService;
import de.otto.edison.jobs.service.JobMetaService;
import de.otto.edison.navigation.NavBar;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static de.otto.edison.jobs.definition.DefaultJobDefinition.manuallyTriggerableJobDefinition;
import static java.time.Duration.ofHours;
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.boot.test.util.EnvironmentTestUtils.addEnvironment;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JobDefinitionsControllerTest {

    private JobDefinitionsController controller;

    @Mock
    private ManagementServerProperties managementServerProperties;

    @Mock
    private NavBar navBar;

    @Mock
    private JobDefinitionService jobDefinitionService;

    @Mock
    private JobMetaService jobMetaService;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(managementServerProperties.getContextPath()).thenReturn("/internal");
        controller = new JobDefinitionsController(jobDefinitionService, jobMetaService, navBar, managementServerProperties);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addPlaceholderValue("management.context-path", "/internal")
                .build();
    }

    @Test
    public void shouldReturn404IfJobDefinitionIsUnknown() throws Exception {
        when(jobDefinitionService.getJobDefinition("FooJob")).thenReturn(Optional.empty());
        mockMvc.perform(get("/internal/jobdefinitions/FooJob"))
                .andExpect(status().is(404));
    }

    @Test
    public void shouldReturnJobDefinitionIfJobExists() throws Exception {
        // given
        final String jobType = "FooJob";
        final JobDefinition expectedDef = jobDefinition(jobType, "Foo");
        when(jobDefinitionService.getJobDefinition(jobType)).thenReturn(Optional.of(expectedDef));

        // when
        mockMvc.perform(
                get("/internal/jobdefinitions/FooJob")
                        .accept("application/json")
        )
                .andExpect(status().is(200))
                .andExpect(content().json("{\"type\":\"FooJob\"," +
                        "\"name\":\"Foo\"," +
                        "\"retries\":0," +
                        "\"fixedDelay\":3600," +
                        "\"links\":[" +
                        "{\"href\":\"/internal/jobsdefinitions/FooJob\",\"rel\":\"self\"},{\"href\":\"/internal/jobdefinitions\",\"rel\":\"collection\"}," +
                        "{\"href\":\"/internal/jobs/FooJob\",\"rel\":\"http://github.com/otto-de/edison/link-relations/job/trigger\"}" +
                        "]" +
                        "}"));
    }

    @Test
    public void shouldReturnAllJobDefinitions() throws Exception {
        // given
        final JobDefinition fooJobDef = jobDefinition("FooJob", "Foo");
        final JobDefinition barJobDef = jobDefinition("BarJob", "Bar");
        when(jobDefinitionService.getJobDefinitions()).thenReturn(asList(fooJobDef, barJobDef));

        // when
        mockMvc.perform(
                get("/internal/jobdefinitions/")
                        .accept("application/json")
        )
                .andExpect(status().is(200))
                .andExpect(content().json("{" +
                        "\"links\":[" +
                        "{\"href\":\"/internal/jobdefinitions/FooJob\",\"rel\":\"http://github.com/otto-de/edison/link-relations/job/definition\",\"title\":\"Foo\"}," +
                        "{\"href\":\"/internal/jobdefinitions/BarJob\",\"rel\":\"http://github.com/otto-de/edison/link-relations/job/definition\",\"title\":\"Bar\"}," +
                        "{\"href\":\"/internal/jobdefinitions\",\"rel\":\"self\",\"title\":\"Self\"}" +
                        "]" +
                        "}"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnAllJobDefinitionsAsHtml() throws Exception {
        // given
        final JobDefinition fooJobDef = jobDefinition("FooJob", "Foo");
        final JobDefinition barJobDef = notTriggerableDefinition("BarJob", "Bar");
        when(jobDefinitionService.getJobDefinitions()).thenReturn(asList(fooJobDef, barJobDef));

        // when
        mockMvc.perform(
                get("/internal/jobdefinitions/")
                        .accept("text/html")
        )
                .andExpect(status().is(200))
                .andDo(result -> {
                    Map<String, Object> model = result.getModelAndView().getModel();
                    List<Map<String, Object>> jobDefinitions = (List<Map<String, Object>>) model.get("jobdefinitions");
                    assertThat(jobDefinitions.size(), is(2));
                    assertThat(jobDefinitions.get(0).get("frequency"), is("Every 60 Minutes"));
                    assertThat(jobDefinitions.get(0).get("isDisabled"), is(false));
                    assertThat(jobDefinitions.get(0).get("comment"), is(""));
                    assertThat(jobDefinitions.get(1).get("frequency"), is("Never"));
                    assertThat(jobDefinitions.get(1).get("isDisabled"), is(false));
                    assertThat(jobDefinitions.get(1).get("comment"), is(""));
                });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertToSecondsIfSecondsIsLessThan60() throws Exception {
        // Given
        final JobDefinition jobDef = jobDefinition("TheJob", "Job", ofSeconds(59));
        when(jobDefinitionService.getJobDefinitions()).thenReturn(singletonList(jobDef));

        // when
        mockMvc.perform(
                get("/internal/jobdefinitions/")
                        .accept("text/html")
        )
                .andExpect(status().is(200))
                .andDo(result -> {
                    List<Map<String, Object>> jobDefinitions = (List<Map<String, Object>>) result.getModelAndView().getModel().get("jobdefinitions");
                    assertThat(jobDefinitions.size(), is(1));
                    assertThat(jobDefinitions.get(0).get("frequency"), is("Every 59 Seconds"));
                });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConvertToMinutesIfSecondsIsNotLessThan60() throws Exception {
        // Given
        final JobDefinition jobDef = jobDefinition("TheJob", "Job", ofSeconds(60));
        when(jobDefinitionService.getJobDefinitions()).thenReturn(singletonList(jobDef));

        // when
        mockMvc.perform(
                get("/internal/jobdefinitions/")
                        .accept("text/html")
        )
                .andExpect(status().is(200))
                .andDo(result -> {
                    List<Map<String, Object>> jobDefinitions = (List<Map<String, Object>>) result.getModelAndView().getModel().get("jobdefinitions");
                    assertThat(jobDefinitions.size(), is(1));
                    assertThat(jobDefinitions.get(0).get("frequency"), is("Every 1 Minutes"));
                });
    }

    private JobDefinition jobDefinition(final String jobType, final String name) {
        return jobDefinition(jobType, name, ofHours(1));
    }

    private JobDefinition jobDefinition(final String jobType, final String name, Duration fixedDelay) {
        return fixedDelayJobDefinition(jobType, name, name, fixedDelay, 0, empty());
    }

    private JobDefinition notTriggerableDefinition(final String jobType, final String name) {
        return manuallyTriggerableJobDefinition(jobType, name, name, 0, empty());
    }

}
