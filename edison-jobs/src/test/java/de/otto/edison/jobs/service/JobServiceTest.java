package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
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
import static java.time.Clock.fixed;
import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
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
    public void shouldReturnCreatedJobId() {
        // given:
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));
        JobService jobService = new JobService(someJobRepository(), emptySet(), asList(jobRunnable), mock(GaugeService.class), executorService, applicationEventPublisher);
        // when:
        Optional<String> jobId = jobService.startAsyncJob("BAR");
        // then:
        assertThat(jobId.isPresent(), is(true));
        assertThat(jobId.get().length(), is(36));
    }

    private JobRepository someJobRepository() {
        JobRepository jobRepository = mock(JobRepository.class);
        when(jobRepository.findRunningJobByType(anyString())).thenReturn(Optional.<JobInfo>empty());
        return jobRepository;
    }

    @Test
    public void shouldRunJob() {
        // given:
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));
        InMemJobRepository jobRepository = new InMemJobRepository();
        JobService jobService = new JobService(
                jobRepository,
                emptySet(),
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
    public void shouldStartAndWaitForJobExecution() {
        // given:
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));
        InMemJobRepository jobRepository = new InMemJobRepository();
        JobService jobService = new JobService(
                jobRepository,
                emptySet(),
                asList(jobRunnable),
                mock(GaugeService.class),
                executorService,
                applicationEventPublisher
        );

        // when:
        jobService.startJob("bar");

        // then:
        verify(jobRunnable).execute(any(JobEventPublisher.class));
        verify(executorService, never()).execute(any(Runnable.class));
    }

    @Test
    public void shouldNotRunSameJobsInParallel() {
        // given:
        Clock clock = fixed(now(), systemDefault());
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));
        InMemJobRepository jobRepository = new InMemJobRepository();
        JobService jobService = new JobService(
                jobRepository,
                emptySet(),
                asList(jobRunnable),
                mock(GaugeService.class),
                executorService,
                applicationEventPublisher
        );
        String alreadyRunningJob = "barIsRunning";
        jobRepository.createOrUpdate(newJobInfo(alreadyRunningJob, "BAR", clock, "localhost"));
        // when:
        Optional<String> jobUri = jobService.startAsyncJob("BAR");
        // then:
        assertThat(jobUri.isPresent(), is(false));
    }

    @Test
    public void shouldRunDifferentJobsInParallel() {
        // given:
        Clock clock = fixed(now(), systemDefault());
        InMemJobRepository jobRepository = new InMemJobRepository();
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("FOO"));
        String alreadyRunningJob ="barIsRunning";
        jobRepository.createOrUpdate(newJobInfo(alreadyRunningJob, "BAR", clock, "localhost"));
        JobService jobService = new JobService(
                jobRepository,
                emptySet(),
                asList(jobRunnable),
                mock(GaugeService.class),
                executorService,
                applicationEventPublisher
        );

        // when:
        Optional<String> jobUri = jobService.startAsyncJob("foo");
        // then:
        assertThat(jobUri.get(), is(not(alreadyRunningJob)));
    }

    @Test
    public void shouldMutuallyExcludeJobs() {
        // given:
        Clock clock = fixed(now(), systemDefault());
        InMemJobRepository jobRepository = new InMemJobRepository();
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("FOO"));
        String alreadyRunningJob = "/internal/jobs/barIsRunning";
        jobRepository.createOrUpdate(newJobInfo(alreadyRunningJob, "BAR", clock, "localhost"));
        JobService jobService = new JobService(
                jobRepository,
                new HashSet<>(asList(new JobMutexGroup("Product Import", "FOO", "BAR"))),
                asList(jobRunnable),
                mock(GaugeService.class),
                executorService,
                applicationEventPublisher
        );

        // when:
        Optional<String> jobUri = jobService.startAsyncJob("FOO");
        // then:
        assertThat(jobUri.isPresent(), is(false));
    }

    @Test
    public void shouldReportRuntime() {
        // given:
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition("BAR"));

        GaugeService mock = mock(GaugeService.class);
        JobRepository repository = someJobRepository(Optional.empty());
        JobService jobService = new JobService(repository, emptySet(), asList(jobRunnable), mock, executorService, applicationEventPublisher);
        // when:
        jobService.startAsyncJob("BAR");
        // then:
        verify(mock).submit(eq("gauge.jobs.runtime.bar"), anyLong());
    }

    private JobRepository someJobRepository(Optional<JobInfo> empty) {
        JobRepository repository = mock(JobRepository.class);
        when(repository.findRunningJobByType(anyString())).thenReturn(empty);
        return repository;
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