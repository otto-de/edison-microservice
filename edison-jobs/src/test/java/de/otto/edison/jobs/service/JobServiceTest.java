package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.DefaultJobDefinition;
import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import de.otto.edison.status.domain.SystemInfo;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Clock;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.status.domain.SystemInfo.systemInfo;
import static java.time.Clock.fixed;
import static java.time.Clock.systemDefaultZone;
import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class JobServiceTest {

    @Mock
    private ScheduledExecutorService executorService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private JobRunnable jobRunnable;
    @Mock
    private JobRepository jobRepository;
    @Mock
    GaugeService gaugeServiceMock;

    JobService jobService;

    private SystemInfo systemInfo;
    private Clock clock;
    private JobInfo jobInfo;


    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        initMocks(this);
        this.systemInfo = systemInfo("HOST", 8080);

        this.clock = fixed(now(), systemDefault());

        doAnswer(new RunImmediately()).when(executorService).execute(any(Runnable.class));
        when(executorService.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(mock(ScheduledFuture.class));
        when(jobRunnable.getJobDefinition()).thenReturn(DefaultJobDefinition.manuallyTriggerableJobDefinition("someType", "bla", "bla", 0, Optional.empty()));
        jobInfo = JobInfo.newJobInfo("someId", "someType", clock, "someHost");
        when(jobRepository.startJob(any(), any())).thenReturn(jobInfo);

        jobService = new JobService(jobRepository, asList(jobRunnable), gaugeServiceMock, executorService, applicationEventPublisher, clock, systemInfo);
        jobService.postConstruct();
    }

    @Test
    public void shouldReturnCreatedJobId() {
        // given:
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));

        // when:
        Optional<String> jobId = jobService.startAsyncJob("BAR");
        // then:
        assertThat(jobId.isPresent(), is(true));
        assertThat(jobId.get(), is("someId"));
    }


    @Test
    public void shouldRunJob() {
        // given:
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));


        // when:
        jobService.startAsyncJob("bar");

        // then:
        verify(executorService).execute(any(Runnable.class));
        verify(jobRunnable).execute(any(JobEventPublisher.class));
    }

    @Test
    public void shouldNotStartJobOnBlockedException() {
        // given:
        JobInfo input =  newJobInfo("", "someType", clock,
                systemInfo.getHostname());

        doThrow(new JobBlockedException("bla")).when(jobRepository).startJob(eq(input), any());

        // when:
        Optional<String> jobUri = jobService.startAsyncJob("someType");
        // then:
        assertThat(jobUri.isPresent(), is(false));
    }

    @Test
    public void shouldReportRuntime() {
        // given:
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));

        // when:
        jobService.startAsyncJob("BAR");
        // then:
        verify(gaugeServiceMock).submit(eq("gauge.jobs.runtime.bar"), anyLong());
    }

    private JobDefinition someJobDefinition(String jobType) {
        return new JobDefinition() {
            @Override
            public String jobType() {
                return jobType;
            }

            @Override
            public String jobName() {
                return "test";
            }

            @Override
            public String description() {
                return "test";
            }
        };
    }

    private static class RunImmediately implements Answer {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Runnable runnable = (Runnable) invocation.getArguments()[0];
            runnable.run();
            return null;
        }
    }
}