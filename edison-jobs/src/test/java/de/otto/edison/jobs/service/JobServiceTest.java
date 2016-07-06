package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.DefaultJobDefinition;
import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.status.domain.SystemInfo;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Clock;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class JobServiceTest {

    public static final String HOSTNAME = "HOST";
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
    @Mock
    JobMutexGroup jobMutexGroup;

    JobService jobService;

    private SystemInfo systemInfo;
    private Clock clock;


    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        initMocks(this);
        this.systemInfo = systemInfo(HOSTNAME, 8080);

        this.clock = fixed(now(), systemDefault());

        doAnswer(new RunImmediately()).when(executorService).execute(any(Runnable.class));
        when(executorService.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(mock(ScheduledFuture.class));
        when(jobRunnable.getJobDefinition()).thenReturn(DefaultJobDefinition.manuallyTriggerableJobDefinition("someType", "bla", "bla", 0, Optional.empty()));

        jobService = new JobService(jobRepository, asList(jobRunnable), gaugeServiceMock, executorService, applicationEventPublisher, clock, systemInfo, set(jobMutexGroup));
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
        verify(executorService).execute(any(Runnable.class));
        verify(jobRepository).createOrUpdate(JobInfo.newJobInfo(optionalJobId.get(), jobType, clock, systemInfo.hostname));
        verify(jobRunnable).execute(any(JobEventPublisher.class));
        verify(jobRepository).markJobAsRunningIfPossible(jobType, set(jobType));
    }



    private <T> Set<T> set(T... values) {
        if(values==null) {
            return emptySet();
        }
        HashSet<T> result = new HashSet<>();
        for(T value: values) {
            result.add(value);
        }
        return result;
    }

    @Test
    public void shouldNotStartJobOnBlockedException() {
        JobInfo input =  newJobInfo("", "someType", clock,
                systemInfo.getHostname());
        doThrow(new JobBlockedException("bla")).when(jobRepository).markJobAsRunningIfPossible(eq(input.getJobType()), any());

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
    public void shouldMergeMutexGroups() {
        // given
        String jobType = "someType";
        JobMutexGroup one = new JobMutexGroup("group1", jobType, "type2", "type3");
        JobMutexGroup two = new JobMutexGroup("group2", jobType, "type2", "type4");
        JobMutexGroup three = new JobMutexGroup("otherGroup", "käse", "wurst", "wurstkäse");
        jobService = new JobService(jobRepository, asList(jobRunnable), gaugeServiceMock, executorService,
                applicationEventPublisher, clock, systemInfo, set(one, two, three)) ;

        // when
        jobService.startAsyncJob(jobType);

        verify(jobRepository).markJobAsRunningIfPossible(jobType, set(jobType, "type2", "type3", "type4"));
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