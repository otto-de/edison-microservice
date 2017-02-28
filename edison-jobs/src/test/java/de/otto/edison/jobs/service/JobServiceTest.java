package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.status.domain.SystemInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.manuallyTriggerableJobDefinition;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.status.domain.SystemInfo.systemInfo;
import static java.time.Clock.fixed;
import static java.time.Clock.offset;
import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class JobServiceTest {

    private static final String HOSTNAME = "HOST";
    private static final String JOB_ID = "JOB/ID";
    private static final String JOB_TYPE = "JOB_TYPE";

    @Mock
    private ScheduledExecutorService executorService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private JobRunnable jobRunnable;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private GaugeService gaugeServiceMock;
    @Mock
    private UuidProvider uuidProviderMock;
    @Mock
    private JobMetaService jobMetaService;
    private JobService jobService;
    private SystemInfo systemInfo;
    private Clock clock;


    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        initMocks(this);
        this.systemInfo = systemInfo(HOSTNAME, 8080);

        this.clock = fixed(now(), systemDefault());

        doAnswer(new RunImmediately())
                .when(executorService)
                .execute(any(Runnable.class));
        when(executorService.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(mock(ScheduledFuture.class));
        when(jobRunnable.getJobDefinition())
                .thenReturn(manuallyTriggerableJobDefinition("someType", "bla", "bla", 0, Optional.empty()));
        when(uuidProviderMock.getUuid())
                .thenReturn(JOB_ID);
        jobService = new JobService(
                jobRepository, jobMetaService, singletonList(jobRunnable),
                gaugeServiceMock, executorService, applicationEventPublisher, clock, systemInfo, uuidProviderMock);
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
        assertThat(jobId.get(), not(isEmptyOrNullString()));
    }


    @Test
    public void shouldRunJob() {
        // given:
        String jobType = "bar";
        when(jobRunnable.getJobDefinition()).thenReturn(someJobDefinition(jobType));

        // when:
        Optional<String> optionalJobId = jobService.startAsyncJob(jobType);

        // then:
        final JobInfo expectedJobInfo = JobInfo.newJobInfo(optionalJobId.get(), jobType, clock, systemInfo.hostname);
        verify(executorService).execute(any(Runnable.class));
        verify(jobRepository).createOrUpdate(expectedJobInfo);
        verify(jobRunnable).execute(any(JobEventPublisher.class));
        verify(jobMetaService).aquireRunLock(expectedJobInfo.getJobId(), expectedJobInfo.getJobType());
    }

    @Test
    public void shouldNotStartJobOnBlockedException() {
        doAnswer((x) -> {throw new JobBlockedException("");})
                .when(jobMetaService).aquireRunLock(anyString(), anyString());

        Optional<String> jobUri = jobService.startAsyncJob("someType");

        assertThat(jobUri.isPresent(), is(false));
        verify(jobRepository, never()).createOrUpdate(any());
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

    @Test
    public void shouldStopJob() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        Clock earlierClock = offset(clock, Duration.of(-1, MINUTES));
        JobInfo jobInfo = JobInfo.newJobInfo("superId", "superType", earlierClock, HOSTNAME);
        when(jobRepository.findOne("superId")).thenReturn(Optional.of(jobInfo));

        jobService.stopJob("superId");

        JobInfo expected = jobInfo.copy().setStatus(JobInfo.JobStatus.OK).setStopped(now).setLastUpdated(now).build();
        verify(jobMetaService).releaseRunLock("superType");
        verify(jobRepository).createOrUpdate(expected);
    }

    @Test
    public void shouldKillJob() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        JobInfo jobInfo = JobInfo.newJobInfo("superId", "superType", clock, HOSTNAME);
        when(jobRepository.findOne("superId")).thenReturn(Optional.of(jobInfo));

        jobService.killJob("superId", "superType");

        JobInfo expected = jobInfo.copy().setStatus(JobInfo.JobStatus.DEAD).setStopped(now).setLastUpdated(now).build();
        verify(jobMetaService).releaseRunLock("superType");
        verify(jobRepository).createOrUpdate(expected);
    }

    @Test
    public void shouldKillDeadJobsSince() {
        JobInfo someJobInfo = defaultJobInfo().setJobType("jobType").build();
        when(jobRepository.findRunningWithoutUpdateSince(any())).thenReturn(singletonList(someJobInfo));
        when(jobRepository.findOne(someJobInfo.getJobId())).thenReturn(Optional.of(someJobInfo));

        jobService.killJobsDeadSince(60);

        verify(jobMetaService).releaseRunLock("jobType");
    }

    @Test
    public void shouldUpdateTimeStampOnKeepAlive() {
        //when
        jobService.keepAlive(JOB_ID);

        //then
        OffsetDateTime now = OffsetDateTime.now(clock);
        verify(jobRepository).setLastUpdate(JOB_ID, now);
    }

    @Test
    public void shouldMarkSkipped() {
        //when
        jobService.markSkipped(JOB_ID);

        // then
        OffsetDateTime now = OffsetDateTime.now(clock);

        verify(jobRepository).appendMessage(JOB_ID, jobMessage(Level.INFO, "Skipped job ..", now));
        verify(jobRepository).setLastUpdate(JOB_ID, now);
        verify(jobRepository).setJobStatus(JOB_ID, JobInfo.JobStatus.SKIPPED);
    }

    @Test
    public void shouldMarkRestarted() {
        //when
        jobService.markRestarted(JOB_ID);

        // then
        OffsetDateTime now = OffsetDateTime.now(clock);

        verify(jobRepository).appendMessage(JOB_ID, jobMessage(Level.WARNING, "Restarting job ..", now));
        verify(jobRepository).setLastUpdate(JOB_ID, now);
        verify(jobRepository).setJobStatus(JOB_ID, JobInfo.JobStatus.OK);
    }

    @Test
    public void shouldAppendNonErrorMessage() {
        JobMessage message = JobMessage.jobMessage(Level.INFO, "This is an interesting message", OffsetDateTime.now());

        // when
        jobService.appendMessage(JOB_ID, message);

        // then
        verify(jobRepository).appendMessage(JOB_ID, message);
        verify(jobRepository, never()).createOrUpdate(any(JobInfo.class));
    }

    @Test
    public void shouldAppendErrorMessageAndSetErrorStatus() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        OffsetDateTime earlier = now.minus(10, MINUTES);
        JobMessage message = JobMessage.jobMessage(Level.ERROR, "Error: Out of hunk", now);
        JobInfo jobInfo = defaultJobInfo()
                .setLastUpdated(earlier)
                .build();
        when(jobRepository.findOne(JOB_ID)).thenReturn(Optional.of(jobInfo));

        // when
        jobService.appendMessage(JOB_ID, message);

        // then
        JobInfo expected = jobInfo.copy()
                .setStatus(JobInfo.JobStatus.ERROR)
                .setLastUpdated(now).build();
        verify(jobRepository).appendMessage(JOB_ID, message);
        verify(jobRepository).createOrUpdate(expected);
    }

    private JobInfo.Builder defaultJobInfo() {
        return newJobInfo(JOB_ID, JOB_TYPE, clock, HOSTNAME).copy();
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

    private static class RunImmediately implements Answer<Object> {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Runnable runnable = (Runnable) invocation.getArguments()[0];
            runnable.run();
            return null;
        }
    }
}
