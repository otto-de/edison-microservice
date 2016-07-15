package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.service.JobService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JobsControllerTest {


    private JobService jobService;
    private MockMvc mockMvc;
    private JobsController jobsController;

    @BeforeMethod
    public void setUp() throws Exception {
        jobService = mock(JobService.class);
        jobsController = new JobsController(jobService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(jobsController)
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
        OffsetDateTime now = OffsetDateTime.now(cet);
        JobInfo expectedJob = newJobInfo("42", "TEST", fixed(now.toInstant(), cet), "localhost");
        when(jobService.findJob("42")).thenReturn(Optional.of(expectedJob));

        String nowAsString = ISO_OFFSET_DATE_TIME.format(now);
        mockMvc.perform(MockMvcRequestBuilders
                .get("/some-microservice/internal/jobs/42")
                .servletPath("/internal/jobs/42"))
                .andExpect(status().is(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.messages").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jobType").value("TEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hostname").value("localhost"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.started").value(nowAsString))
                .andExpect(MockMvcResultMatchers.jsonPath("$.stopped").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastUpdated").value(nowAsString))
                .andExpect(MockMvcResultMatchers.jsonPath("$.jobUri").value("http://localhost/some-microservice/internal/jobs/42"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.links").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.links[0].href").value("http://localhost/some-microservice/internal/jobs/42"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.links[1].href").value("http://localhost/some-microservice/internal/jobdefinitions/TEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.links[2].href").value("http://localhost/some-microservice/internal/jobs"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.links[3].href").value("http://localhost/some-microservice/internal/jobs?type=TEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.runtime").value(""))
                .andExpect(MockMvcResultMatchers.jsonPath("$.state").value("Running"));
        verify(jobService).findJob("42");
    }

    @Test
    public void shouldReturnAllJobs() throws IOException {
        // given
        JobInfo firstJob = newJobInfo("42", "TEST", fixed(ofEpochMilli(0), systemDefault()), "localhost");
        JobInfo secondJob = newJobInfo("42", "TEST", fixed(ofEpochMilli(1), systemDefault()), "localhost");
        when(jobService.findJobs(Optional.<String>empty(), 100)).thenReturn(asList(firstJob, secondJob));

        // when
        Object job = jobsController.getJobsAsJson(null, 100, false, mock(HttpServletRequest.class));

        // then
        assertThat(job, is(asList(representationOf(firstJob, false, ""), representationOf(secondJob, false, ""))));
    }

    @Test
    public void shouldReturnAllJobsDistinct() throws IOException {
        // given
        JobInfo firstJob = newJobInfo("42", "jobType1", fixed(ofEpochMilli(0), systemDefault()), "localhost");
        JobInfo secondJob = newJobInfo("42", "jobType2", fixed(ofEpochMilli(1), systemDefault()), "localhost");
        JobInfo thirdJob = newJobInfo("42", "jobType3", fixed(ofEpochMilli(2), systemDefault()), "localhost");

        when(jobService.findJobsDistinct()).thenReturn(asList(firstJob, secondJob, thirdJob));

        // when
        Object job = jobsController.getJobsAsJson(null, 100, true, mock(HttpServletRequest.class));

        // then
        assertThat(job, is(asList(representationOf(firstJob, false, ""),
                representationOf(secondJob, false, ""),
                representationOf(thirdJob, false, ""))));

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
        assertThat(jobs, is(asList(representationOf(firstJob, false, ""))));
    }

    @Test
    public void shouldTriggerJobAndReturnItsURL() throws Exception {
        when(jobService.startAsyncJob("someJobType")).thenReturn(Optional.of("theJobId"));

        mockMvc.perform(MockMvcRequestBuilders
                .post("/some-microservice/internal/jobs/someJobType")
                .servletPath("/internal/jobs/someJobType"))
                .andExpect(status().is(204))
                .andExpect(MockMvcResultMatchers.header().string("Location", "http://localhost/some-microservice/internal/jobs/theJobId"));

        verify(jobService).startAsyncJob("someJobType");
    }

    @Test
    public void shouldDisableJob() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .post("/some-microservice/internal/jobs/someJobType/disable"))
                .andExpect(status().is(SC_NO_CONTENT));

        verify(jobService).disableJobType("someJobType");
    }
}
