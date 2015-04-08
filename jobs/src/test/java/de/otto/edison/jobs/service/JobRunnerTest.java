package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.JobType;
import de.otto.edison.jobs.repository.InMemJobRepository;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.testsupport.util.TestClock;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.domain.Level.INFO;
import static de.otto.edison.jobs.service.JobRunner.PING_PERIOD;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isPresent;
import static java.net.URI.create;
import static java.time.Clock.fixed;
import static java.time.Clock.tick;
import static java.time.Clock.tickSeconds;
import static java.time.Duration.of;
import static java.time.Duration.ofSeconds;
import static java.time.OffsetDateTime.now;
import static java.time.OffsetDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class JobRunnerTest {

    private Clock clock;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture scheduledJob;

    @BeforeMethod
    public void setUp() throws Exception {
        clock = fixed(Instant.now(), systemDefault());
        this.scheduledExecutorService = mock(ScheduledExecutorService.class);

        scheduledJob = mock(ScheduledFuture.class);
        doReturn(scheduledJob)
                .when(scheduledExecutorService).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void shouldExecuteJob() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobRunner jobRunner = newJobRunner(jobInfoBuilder(()->"NAME", jobUri).build(), repository, clock, scheduledExecutorService);
        // when
        jobRunner.start(new SomeJobRunnable());
        // then
        final JobInfo jobInfo = repository.findBy(jobUri).get();
        assertThat(jobInfo.getStatus(), is(OK));
        assertThat(jobInfo.getStopped(), isPresent());
    }

    @Test
    public void shouldPersistJobInfo() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobRunner jobRunner = newJobRunner(jobInfoBuilder(() -> "NAME", jobUri).build(), repository, clock, scheduledExecutorService);
        // when
        jobRunner.start(new SomeJobRunnable());
        // then
        final Optional<JobInfo> optionalJob = repository.findBy(jobUri);
        assertThat(optionalJob, isPresent());
    }

    @Test
    public void shouldAddMessageToJobInfo() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobRunner jobRunner = newJobRunner(jobInfoBuilder(() -> "NAME", jobUri).build(), repository, clock, scheduledExecutorService);
        // when
        jobRunner.start(new SomeJobRunnable());
        // then
        final Optional<JobInfo> optionalJob = repository.findBy(jobUri);
        final JobInfo jobInfo = optionalJob.get();
        assertThat(jobInfo.getMessages(), hasSize(1));
        final JobMessage message = jobInfo.getMessages().get(0);
        assertThat(message.getMessage(), is("a message"));
        assertThat(message.getLevel(), is(INFO));
        assertThat(message.getTimestamp(), is(notNullValue()));
    }

    @Test
    public void shouldUpdateJobTimeStamp() {
        //given
        final URI jobUri = create("/foo/jobs/42");
        final JobRepository repository = mock(JobRepository.class);

        OffsetDateTime startedTime = ofInstant(Instant.ofEpochMilli(0L), systemDefault());
        OffsetDateTime loggingTime = startedTime.plusSeconds(1);
        OffsetDateTime finishTime = loggingTime.plusSeconds(1);

        clock = mock(Clock.class);
        when(clock.getZone()).thenReturn(systemDefault());
        when(clock.instant()).thenReturn(Instant.ofEpochSecond(0L),Instant.ofEpochSecond(1L),Instant.ofEpochSecond(2L));

        final JobRunner jobRunner = newJobRunner(jobInfoBuilder(() -> "NAME", jobUri).build(), repository, clock, scheduledExecutorService);
        // when
        jobRunner.start(new SomeJobRunnable());
        //then
        List<JobInfo> jobInfoHistory = historyOfSavedJobInfos(repository, 3);

        assertThat(jobInfoHistory.get(0).getLastUpdated(),is(startedTime));
        assertThat(jobInfoHistory.get(1).getLastUpdated(),is(loggingTime));
        assertThat(jobInfoHistory.get(2).getLastUpdated(),is(finishTime));
    }

    @Test
    public void shouldPeriodicallyUpdateJobTimestampSoThatWeCanDetectDeadJobs() {
        //given
        TestClock testClock = TestClock.now();
        final URI jobUri = create("/foo/jobs/42");
        final JobRepository repository = mock(JobRepository.class);
        final JobRunner jobRunner = newJobRunner(jobInfoBuilder(() -> "NAME", jobUri).build(), repository, testClock, scheduledExecutorService);
        // when
        jobRunner.start(new SomeJobRunnable());
        //then

        ArgumentCaptor<Runnable> pingRunnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduledExecutorService).scheduleAtFixedRate(pingRunnableArgumentCaptor.capture(), eq(PING_PERIOD), eq(PING_PERIOD), eq(SECONDS));

        // given
        reset(repository);
        testClock.proceed(1, MINUTES);
        // when
        pingRunnableArgumentCaptor.getValue().run();
        // then
        List<JobInfo> historyOfSavedJobInfos = historyOfSavedJobInfos(repository, 1);
        assertThat(historyOfSavedJobInfos.get(0).getLastUpdated().toInstant(), is(testClock.instant()));
    }

    @Test
    public void shouldStopPeriodicallyUpdateJobTimestampWhenJobIsFinished() {
        //given

        final URI jobUri = create("/foo/jobs/42");
        final JobRepository repository = mock(JobRepository.class);
        final JobRunner jobRunner = newJobRunner(jobInfoBuilder(() -> "NAME", jobUri).build(), repository, clock, scheduledExecutorService);
        // when
        jobRunner.start(new SomeJobRunnable());

        //then
        verify(scheduledJob).cancel(false);
    }


    private List<JobInfo> historyOfSavedJobInfos(JobRepository repository, int wantedNumberOfInvocations) {
        ArgumentCaptor<JobInfo> jobInfoArgumentCaptor = ArgumentCaptor.forClass(JobInfo.class);
        verify(repository,times(wantedNumberOfInvocations)).createOrUpdate(jobInfoArgumentCaptor.capture());

        return jobInfoArgumentCaptor.getAllValues();
    }

    private static class SomeJobRunnable implements JobRunnable {
        @Override
        public JobType getJobType() {
            return () -> "NAME";
        }

        @Override
        public void execute(final JobLogger logger) {
            logger.log(jobMessage(INFO, "a message"));
        }
    }
}