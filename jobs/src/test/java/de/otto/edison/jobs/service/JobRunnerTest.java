package de.otto.edison.jobs.service;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.monitor.JobMonitor;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import de.otto.edison.testsupport.util.TestClock;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.DEAD;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.domain.Level.ERROR;
import static de.otto.edison.jobs.domain.Level.INFO;
import static de.otto.edison.jobs.domain.Level.WARNING;
import static de.otto.edison.jobs.service.JobRunner.PING_PERIOD;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isPresent;
import static java.net.URI.create;
import static java.time.Clock.fixed;
import static java.time.Duration.ofSeconds;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class JobRunnerTest {

    private Clock clock;
    private ScheduledExecutorService executor;
    private ScheduledFuture scheduledJob;

    @BeforeMethod
    public void setUp() throws Exception {
        clock = fixed(Instant.now(), systemDefault());
        this.executor = mock(ScheduledExecutorService.class);

        scheduledJob = mock(ScheduledFuture.class);
        doReturn(scheduledJob)
                .when(executor).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void shouldExecuteJob() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobInfo jobInfo = newJobInfo(jobUri, "NAME", (j) -> {}, clock);
        final JobRunner jobRunner = newJobRunner(jobInfo, repository, executor);
        // when
        JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobDefinition()).thenReturn(fixedDelayJobDefinition("NAME", "", "", ofSeconds(2), 0, empty()));
        jobRunner.start(jobRunnable);
        // then
        verify(jobRunnable).execute(jobInfo);
        final JobInfo persistedJobInfo = repository.findOne(jobUri).get();
        assertThat(persistedJobInfo.getStatus(), is(OK));
        assertThat(persistedJobInfo.getStopped(), isPresent());
    }

    @Test
    public void shouldAddMessageToJobInfo() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobRunner jobRunner = newJobRunner(
                newJobInfo(jobUri, "NAME", (j) -> {
                }, clock),
                repository,
                executor);
        // when
        jobRunner.start(new SomeJobRunnable());
        // then
        final Optional<JobInfo> optionalJob = repository.findOne(jobUri);
        final JobInfo jobInfo = optionalJob.get();
        assertThat(jobInfo.getMessages(), hasSize(2));
        final JobMessage first = jobInfo.getMessages().get(0);
        final JobMessage second = jobInfo.getMessages().get(1);
        assertThat(first.getMessage(), is("Started NAME"));
        assertThat(second.getMessage(), is("a message"));
        assertThat(second.getLevel(), is(INFO));
        assertThat(second.getTimestamp(), is(notNullValue()));
    }

    @Test
    public void shouldRestartJobOnException() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobRunner jobRunner = newJobRunner(
                newJobInfo(jobUri, "NAME", (j) -> {
                }, clock),
                repository,
                executor);

        final SomeJobRunnable someFailingJob = new SomeJobRunnable(j -> {
            throw new RuntimeException("some error");
        });

        // when
        jobRunner.start(someFailingJob);

        // then
        final Optional<JobInfo> optionalJob = repository.findOne(jobUri);
        final JobInfo jobInfo = optionalJob.get();

        // JobRunnable.execute was called 2 times (1 retry):
        assertThat(someFailingJob.executions, is(2));

        final JobMessage startedMessage = jobInfo.getMessages().get(0);
        assertThat(startedMessage.getMessage(), is("Started NAME"));

        final JobMessage firstError = jobInfo.getMessages().get(1);
        assertThat(firstError.getMessage(), is("some error"));
        assertThat(firstError.getLevel(), is(ERROR));

        final JobMessage restartedMessage = jobInfo.getMessages().get(2);
        assertThat(restartedMessage.getMessage(), is("1. restart of Job after error."));
        assertThat(restartedMessage.getLevel(), is(WARNING));

        final JobMessage secondError = jobInfo.getMessages().get(3);
        assertThat(secondError.getMessage(), is("some error"));
        assertThat(secondError.getLevel(), is(ERROR));

        assertThat(jobInfo.getMessages(), hasSize(4));
    }

    @Test
    public void shouldRestartJobOnError() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobRunner jobRunner = newJobRunner(
                newJobInfo(jobUri, "NAME", (j) -> {
                }, clock),
                repository,
                executor);

        final SomeJobRunnable someFailingJob = new SomeJobRunnable(j -> {
            j.error("some error");
            return null;
        });

        // when
        jobRunner.start(someFailingJob);

        // then
        final Optional<JobInfo> optionalJob = repository.findOne(jobUri);
        final JobInfo jobInfo = optionalJob.get();

        // JobRunnable.execute was called 2 times (1 retry):
        assertThat(someFailingJob.executions, is(2));

        final JobMessage startedMessage = jobInfo.getMessages().get(0);
        assertThat(startedMessage.getMessage(), is("Started NAME"));

        final JobMessage firstError = jobInfo.getMessages().get(1);
        assertThat(firstError.getMessage(), is("some error"));
        assertThat(firstError.getLevel(), is(ERROR));

        final JobMessage restartedMessage = jobInfo.getMessages().get(2);
        assertThat(restartedMessage.getMessage(), is("1. restart of Job after error."));
        assertThat(restartedMessage.getLevel(), is(WARNING));

        final JobMessage secondError = jobInfo.getMessages().get(3);
        assertThat(secondError.getMessage(), is("some error"));
        assertThat(secondError.getLevel(), is(ERROR));

        assertThat(jobInfo.getMessages(), hasSize(4));
    }

    @Test
    public void shouldUpdateJobTimeStamp() {
        //given
        final URI jobUri = create("/foo/jobs/42");
        final JobRepository repository = mock(JobRepository.class);

        clock = mock(Clock.class);
        when(clock.getZone()).thenReturn(systemDefault());
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(0L), Instant.ofEpochSecond(1L), Instant.ofEpochSecond(2L));

        // when
        final JobRunner jobRunner = newJobRunner(JobInfo.newJobInfo(jobUri, "JOBTYPE", mock(JobMonitor.class), clock), repository, executor);

        // then
        verify(repository, times(1)).createOrUpdate(any(JobInfo.class));

        // when
        jobRunner.start(new SomeJobRunnable());

        // then
        verify(repository, times(2)).createOrUpdate(any(JobInfo.class));
    }

    @Test
    public void shouldPeriodicallyUpdateJobTimestampSoThatWeCanDetectDeadJobs() {
        //given
        TestClock testClock = TestClock.now();
        final URI jobUri = create("/foo/jobs/42");
        final JobRepository repository = mock(JobRepository.class);
        final JobRunner jobRunner = newJobRunner(
                newJobInfo(jobUri, "NAME", (j) -> {
                }, testClock),
                repository,
                executor);
        // when
        jobRunner.start(new SomeJobRunnable());
        //then

        ArgumentCaptor<Runnable> pingRunnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).scheduleAtFixedRate(pingRunnableArgumentCaptor.capture(), eq(PING_PERIOD), eq(PING_PERIOD), eq(SECONDS));

        // given
        reset(repository);

        when(repository.findStatus(jobUri)).thenReturn(OK);

        testClock.proceed(1, MINUTES);
        // when
        pingRunnableArgumentCaptor.getValue().run();
        // then
        List<JobInfo> historyOfSavedJobInfos = historyOfSavedJobInfos(repository, 1);
        assertThat(historyOfSavedJobInfos.get(0).getLastUpdated().toInstant(), is(testClock.instant()));
    }

    @Test
    public void shouldUpdateJobInfoIfJobIsDead() throws Exception {
        //given
        TestClock testClock = TestClock.now();
        final URI jobUri = create("/foo/jobs/42");
        final JobRepository repository = mock(JobRepository.class);
        JobInfo jobInfo = newJobInfo(jobUri, "NAME", (j) -> {
        }, testClock);
        final JobRunner jobRunner = newJobRunner(
                jobInfo,
                repository,
                executor);
        // when
        jobRunner.start(new SomeJobRunnable());
        //then

        ArgumentCaptor<Runnable> pingRunnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).scheduleAtFixedRate(pingRunnableArgumentCaptor.capture(), eq(PING_PERIOD), eq(PING_PERIOD), eq(SECONDS));

        // given
        reset(repository);

        when(repository.findStatus(jobUri)).thenReturn(DEAD);

        testClock.proceed(1, MINUTES);
        // when
        pingRunnableArgumentCaptor.getValue().run();
        // then

        assertThat(jobInfo.getStatus(), is(DEAD));
        verify(repository).createOrUpdate(jobInfo);
    }

    @Test
    public void shouldStopPeriodicallyUpdateJobTimestampWhenJobIsFinished() {
        //given

        final URI jobUri = create("/foo/jobs/42");
        final JobRepository repository = mock(JobRepository.class);
        final JobRunner jobRunner = newJobRunner(
                newJobInfo(jobUri, "NAME", (j) -> {
                }, clock),
                repository,
                executor);

        // when
        jobRunner.start(new SomeJobRunnable());

        //then
        verify(scheduledJob).cancel(false);
    }

    private List<JobInfo> historyOfSavedJobInfos(JobRepository repository, int wantedNumberOfInvocations) {
        ArgumentCaptor<JobInfo> jobInfoArgumentCaptor = ArgumentCaptor.forClass(JobInfo.class);
        verify(repository, times(wantedNumberOfInvocations)).createOrUpdate(jobInfoArgumentCaptor.capture());

        return jobInfoArgumentCaptor.getAllValues();
    }

    private static class SomeJobRunnable implements JobRunnable {

        final Function<JobInfo, Void> execution;
        int executions = 0;

        SomeJobRunnable() {
            execution = jobInfo -> {
                jobInfo.info("a message");
                return null;
            };
        }

        SomeJobRunnable(Function<JobInfo, Void> execution) {
            this.execution = execution;
        }

        @Override
        public JobDefinition getJobDefinition() {
            return someJobDefinition();
        }

        @Override
        public void execute(final JobInfo jobInfo) {
            ++executions;
            execution.apply(jobInfo);
        }
    }

    private static JobDefinition someJobDefinition() {
        return new JobDefinition() {
            @Override
            public String jobType() {
                return "SOME_TYPE";
            }

            @Override
            public String jobName() {
                return "NAME";
            }

            @Override
            public String description() {
                return "";
            }

            @Override
            public int restarts() {
                return 1;
            }
        };
    }
}