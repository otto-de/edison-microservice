package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.time.Clock.fixed;
import static java.time.ZoneId.systemDefault;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class JobServiceTest {

    private ScheduledExecutorService executorService;
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        executorService = mock(ScheduledExecutorService.class);
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        doAnswer(new RunImmediately()).when(executorService).execute(any(Runnable.class));

        when(executorService.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(mock(ScheduledFuture.class));
    }

    @Test
    public void shouldReturnCreatedJobUri() {
        // given:
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));
        JobRepository jobRepository = mock(JobRepository.class);
        when(jobRepository.findRunningJobByType(anyString())).thenReturn(Optional.<JobInfo>empty());
        JobService jobService = new JobService(jobRepository, asList(jobRunnable), mock(GaugeService.class), executorService, applicationEventPublisher);
        // when:
        Optional<URI> jobUri = jobService.startAsyncJob("BAR");
        // then:
        assertThat(jobUri.get().toString(), startsWith("/internal/jobs/"));
    }

    @Test
    public void shouldRunJob() {
        // given:
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));
        InMemJobRepository jobRepository = new InMemJobRepository();
        JobService jobService = new JobService(
                jobRepository,
                asList(jobRunnable),
                mock(GaugeService.class),
                executorService,
                applicationEventPublisher
        );

        // when:
        jobService.startAsyncJob("bar");

        // then:
        verify(executorService).execute(any(Runnable.class));
    }

    @Test
    public void shouldNotRunSameJobsInParallel() {
        // given:
        Clock clock = fixed(Instant.now(), systemDefault());
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));
        InMemJobRepository jobRepository = new InMemJobRepository();
        JobService jobService = new JobService(
                jobRepository,
                asList(jobRunnable),
                mock(GaugeService.class),
                executorService,
                applicationEventPublisher
        );
        URI alreadyRunningJob = URI.create("/internal/jobs/barIsRunning");
        jobRepository.createOrUpdate(JobInfo.newJobInfo(alreadyRunningJob, "BAR", clock, "localhost"));
        // when:
        Optional<URI> jobUri = jobService.startAsyncJob("bar");
        // then:
        assertThat(jobUri.isPresent(), is(false));
    }

    @Test
    public void shouldRunDifferentJobsInParallel() {
        // given:
        Clock clock = fixed(Instant.now(), systemDefault());
        InMemJobRepository jobRepository = new InMemJobRepository();
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("FOO"));
        URI alreadyRunningJob = URI.create("/internal/jobs/barIsRunning");
        jobRepository.createOrUpdate(JobInfo.newJobInfo(alreadyRunningJob, "BAR", clock, "localhost"));
        JobService jobService = new JobService(
                jobRepository,
                asList(jobRunnable),
                mock(GaugeService.class),
                executorService,
                applicationEventPublisher
        );

        // when:
        Optional<URI> jobUri = jobService.startAsyncJob("foo");
        // then:
        assertThat(jobUri.get(), is(not(alreadyRunningJob)));
    }

    @Test
    public void shouldReportRuntime() {
        // given:
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));

        GaugeService mock = mock(GaugeService.class);
        JobService jobService = new JobService(mock(JobRepository.class), asList(jobRunnable), mock, executorService, applicationEventPublisher);
        // when:
        jobService.startAsyncJob("BAR");
        // then:
        verify(mock).submit(eq("gauge.jobs.runtime.bar"), anyLong());
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