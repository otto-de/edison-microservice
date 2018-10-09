package de.otto.edison.jobs.controller;

import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.service.JobDefinitionService;
import de.otto.edison.jobs.service.JobMetaService;
import de.otto.edison.navigation.NavBar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JobDefinitionsControllerTest {

    private JobDefinitionsController controller;

    private EdisonApplicationProperties webEndpointProperties = new EdisonApplicationProperties();

    @Mock
    private NavBar navBar;

    @Mock
    private JobDefinitionService jobDefinitionService;

    @Mock
    private JobMetaService jobMetaService;

    private MockMvc mockMvc;

    private static String MANAGEMENT_CONTEXT = "/someManagementContext";

    @BeforeEach
    public void setUp() throws Exception {
        initMocks(this);
        webEndpointProperties.getManagement().setBasePath(MANAGEMENT_CONTEXT);
        controller = new JobDefinitionsController(jobDefinitionService, jobMetaService, navBar, webEndpointProperties);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addPlaceholderValue("edison.application.management.base-path", MANAGEMENT_CONTEXT)
                .build();
    }

    @Test
    public void shouldReturn404IfJobDefinitionIsUnknown() throws Exception {
        when(jobDefinitionService.getJobDefinition("FooJob")).thenReturn(Optional.empty());
        mockMvc.perform(get(MANAGEMENT_CONTEXT + "/jobdefinitions/FooJob"))
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
                get(MANAGEMENT_CONTEXT + "/jobdefinitions/FooJob")
                        .accept("application/json")
        )
                .andExpect(status().is(200))
                .andExpect(content().json("{\n" +
                        "  \"type\": \"FooJob\",\n" +
                        "  \"name\": \"Foo\",\n" +
                        "  \"retries\": 0,\n" +
                        "  \"fixedDelay\": 3600,\n" +
                        "  \"links\": [\n" +
                        "    {\n" +
                        "      \"href\": \"" + MANAGEMENT_CONTEXT + "/jobsdefinitions/FooJob\",\n" +
                        "      \"rel\": \"self\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"href\": \"" + MANAGEMENT_CONTEXT + "/jobdefinitions\",\n" +
                        "      \"rel\": \"collection\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"href\": \"" + MANAGEMENT_CONTEXT + "/jobs/FooJob\",\n" +
                        "      \"rel\": \"http://github.com/otto-de/edison/link-relations/job/trigger\"\n" +
                        "    }\n" +
                        "  ]\n" +
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
                get(MANAGEMENT_CONTEXT + "/jobdefinitions/")
                        .accept("application/json")
        )
                .andExpect(status().is(200))
                .andExpect(content().json("{\n" +
                        "  \"links\": [\n" +
                        "    {\n" +
                        "      \"href\": \"" + MANAGEMENT_CONTEXT + "/jobdefinitions/FooJob\",\n" +
                        "      \"rel\": \"http://github.com/otto-de/edison/link-relations/job/definition\",\n" +
                        "      \"title\": \"Foo\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"href\": \"" + MANAGEMENT_CONTEXT + "/jobdefinitions/BarJob\",\n" +
                        "      \"rel\": \"http://github.com/otto-de/edison/link-relations/job/definition\",\n" +
                        "      \"title\": \"Bar\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"href\": \"" + MANAGEMENT_CONTEXT + "/jobdefinitions\",\n" +
                        "      \"rel\": \"self\",\n" +
                        "      \"title\": \"Self\"\n" +
                        "    }\n" +
                        "  ]\n" +
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
                get(MANAGEMENT_CONTEXT + "/jobdefinitions/")
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
                get(MANAGEMENT_CONTEXT + "/jobdefinitions/")
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
                get(MANAGEMENT_CONTEXT + "/jobdefinitions/")
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
