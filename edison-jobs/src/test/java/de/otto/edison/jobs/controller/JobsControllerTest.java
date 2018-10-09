package de.otto.edison.jobs.controller;

import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.service.JobMetaService;
import de.otto.edison.jobs.service.JobService;
import de.otto.edison.navigation.NavBar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.controller.JobRepresentation.representationOf;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.time.Clock.fixed;
import static java.time.Clock.systemDefaultZone;
import static java.time.Instant.ofEpochMilli;
import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.util.Arrays.asList;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_TEMPORARILY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JobsControllerTest {

    @Mock
    private JobService jobService;

    @Mock
    private JobMetaService jobMetaService;

    @Mock
    private NavBar navBar;

    private EdisonApplicationProperties applicationProperties = new EdisonApplicationProperties();

    private MockMvc mockMvc;
    private JobsController jobsController;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        jobsController = new JobsController(jobService, jobMetaService, navBar, applicationProperties);
        mockMvc = MockMvcBuilders
                .standaloneSetup(jobsController)
                .addPlaceholderValue("edison.application.management.base-path", "/internal")
                .defaultRequest(MockMvcRequestBuilders.get("/").contextPath("/some-microservice"))
                .build();
    }

    @Test
    public void shouldReturn404IfJobIsUnknown() throws Exception {
        // given
        when(jobService.findJob(any(String.class))).thenReturn(Optional.<JobInfo>empty());

        // when
        mockMvc.perform(MockMvcRequestBuilders
                .get("/some-microservice/internal/jobs/42"))
                .andExpect(status().is(404));
    }

    @Test
    public void shouldReturnJobIfJobExists() throws Exception {
        // given
        ZoneId cet = ZoneId.of("CET");
        OffsetDateTime now = OffsetDateTime.now(cet).truncatedTo(ChronoUnit.MILLIS);
        JobInfo expectedJob = newJobInfo("42", "TEST", fixed(now.toInstant(), cet), "localhost");
        when(jobService.findJob("42")).thenReturn(Optional.of(expectedJob));

        String nowAsString = ISO_OFFSET_DATE_TIME.format(now);
        mockMvc.perform(MockMvcRequestBuilders
                .get("/some-microservice/internal/jobs/42")
                .servletPath("/internal/jobs/42"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.messages").isArray())
                .andExpect(jsonPath("$.jobType").value("TEST"))
                .andExpect(jsonPath("$.hostname").value("localhost"))
                .andExpect(jsonPath("$.started").value(nowAsString))
                .andExpect(jsonPath("$.stopped").value(""))
                .andExpect(jsonPath("$.lastUpdated").value(nowAsString))
                .andExpect(jsonPath("$.jobUri").value("http://localhost/some-microservice/internal/jobs/42"))
                .andExpect(jsonPath("$.links").isArray())
                .andExpect(jsonPath("$.links[0].href").value("http://localhost/some-microservice/internal/jobs/42"))
                .andExpect(jsonPath("$.links[1].href").value("http://localhost/some-microservice/internal/jobdefinitions/TEST"))
                .andExpect(jsonPath("$.links[2].href").value("http://localhost/some-microservice/internal/jobs"))
                .andExpect(jsonPath("$.links[3].href").value("http://localhost/some-microservice/internal/jobs?type=TEST"))
                .andExpect(jsonPath("$.runtime").value("00:00:00"))
                .andExpect(jsonPath("$.state").value("Running"));
        verify(jobService).findJob("42");
    }

    @Test
    public void shouldReturnAllJobs() throws IOException {
        // given
        JobInfo firstJob = newJobInfo("42", "TEST", fixed(ofEpochMilli(0), systemDefault()), "localhost");
        JobInfo secondJob = newJobInfo("42", "TEST", fixed(ofEpochMilli(1), systemDefault()), "localhost");
        when(jobService.findJobs(Optional.<String>empty(), 100)).thenReturn(asList(firstJob, secondJob));

        // when
        Object job = jobsController.getJobsAsJson(null, 100, false, false, mock(HttpServletRequest.class));

        // then
        assertThat(job, is(asList(representationOf(firstJob, null, false, "", ""), representationOf(secondJob, null, false, "", ""))));
    }

    @Test
    public void shouldNotReturnAllJobsDistinctIfTypeIsGiven() throws IOException {
        // given
        JobInfo firstJob = newJobInfo("11", "jobType1", fixed(ofEpochMilli(0), systemDefault()), "localhost");
        JobInfo secondJob = newJobInfo("12", "jobType2", fixed(ofEpochMilli(1), systemDefault()), "localhost");
        JobInfo thirdJob = newJobInfo("13", "jobType3", fixed(ofEpochMilli(2), systemDefault()), "localhost");
        JobInfo fourthJob = newJobInfo("14", "jobType2", fixed(ofEpochMilli(2), systemDefault()), "localhost");

        when(jobService.findJobs(Optional.of("jobType2"), 100)).thenReturn(asList(secondJob, fourthJob));

        // when
        Object job = jobsController.getJobsAsJson("jobType2", 100, true, false, mock(HttpServletRequest.class));

        // then
        assertThat(job, is(asList(representationOf(secondJob, null, false, "", ""), representationOf(fourthJob, null, false, "", ""))));

        verify(jobService, times(1)).findJobs(Optional.of("jobType2"), 100);
        verifyNoMoreInteractions(jobService);
    }

    @Test
    public void shouldReturnAllJobsDistinct() throws IOException {
        // given
        JobInfo firstJob = newJobInfo("42", "jobType1", fixed(ofEpochMilli(0), systemDefault()), "localhost");
        JobInfo secondJob = newJobInfo("42", "jobType2", fixed(ofEpochMilli(1), systemDefault()), "localhost");
        JobInfo thirdJob = newJobInfo("42", "jobType3", fixed(ofEpochMilli(2), systemDefault()), "localhost");

        when(jobService.findJobsDistinct()).thenReturn(asList(firstJob, secondJob, thirdJob));

        // when
        Object job = jobsController.getJobsAsJson(null, 100, true, false, mock(HttpServletRequest.class));

        // then
        assertThat(job, is(asList(representationOf(firstJob, null, false, "", ""),
                representationOf(secondJob, null, false, "", ""),
                representationOf(thirdJob, null, false, "", ""))));

        verify(jobService, times(1)).findJobsDistinct();
        verifyNoMoreInteractions(jobService);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnAllJobsOfTypeAsHtml() {
        JobInfo firstJob = newJobInfo("42", "SOME_TYPE", systemDefaultZone(), "localhost");
        when(jobService.findJobs(Optional.of("SOME_TYPE"), 100)).thenReturn(asList(firstJob));

        ModelAndView modelAndView = jobsController.getJobsAsHtml("SOME_TYPE", 100, false, mock(HttpServletRequest.class));
        List<JobRepresentation> jobs = (List<JobRepresentation>) modelAndView.getModel().get("jobs");
        assertThat(jobs, is(asList(representationOf(firstJob, null, false, "", ""))));
    }

    @Test
    public void shouldTriggerJobAndReturnItsURL() throws Exception {
        when(jobService.startAsyncJob("someJobType")).thenReturn(Optional.of("theJobId"));

        mockMvc.perform(MockMvcRequestBuilders
                .post("/some-microservice/internal/jobs/someJobType")
                .servletPath("/internal/jobs/someJobType"))
                .andExpect(status().is(204))
                .andExpect(header().string("Location", "http://localhost/some-microservice/internal/jobs/theJobId"));

        verify(jobService).startAsyncJob("someJobType");
    }

    @Test
    public void shouldDisableJob() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .post("/some-microservice/internal/jobs/someJobType/disable"))
                .andExpect(status().is(SC_MOVED_TEMPORARILY))
                .andExpect(header().string("Location", "/some-microservice/internal/jobdefinitions"));

        verify(jobMetaService).disable("someJobType", null);
    }
}
