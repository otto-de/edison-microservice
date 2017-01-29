package de.otto.edison.jobs.repository.cleanup;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.RunningJobs;
import de.otto.edison.jobs.repository.JobLockRepository;
import de.otto.edison.jobs.repository.JobRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClearDeadLocksTest {

    public static final String JOB_TYPE = "someType";
    public static final String JOB_ID = "someId";
    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobLockRepository jobLockRepository;

    private ClearDeadLocks subject;

    private Clock fixedClock;
    private OffsetDateTime now;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        subject = new ClearDeadLocks(jobLockRepository, jobRepository);

        RunningJobs jobs = new RunningJobs(asList(new RunningJobs.RunningJob(JOB_ID, JOB_TYPE)));
        when(jobLockRepository.runningJobs()).thenReturn(jobs);
        now = OffsetDateTime.now(fixedClock);
    }

    @Test
    public void shouldFindAndRemoveLockOfStoppedJob() {
        JobInfo stoppedJob = jobInfo(Optional.of(this.now));
        when(jobRepository.findOne(JOB_ID)).thenReturn(Optional.of(stoppedJob));

        subject.clearLocks();

        verify(jobLockRepository).clearRunningMark(JOB_TYPE);
    }

    @Test
    public void shouldNotClearLockOfStillRunningJob() {
        JobInfo runningJob = jobInfo(Optional.empty());
        when(jobRepository.findOne(JOB_ID)).thenReturn(Optional.of(runningJob));

        subject.clearLocks();

        verify(jobLockRepository, never()).clearRunningMark(JOB_TYPE);
    }

    @Test
    public void shouldClearLockIfNoJobInfoExists() {
        when(jobRepository.findOne(JOB_ID)).thenReturn(Optional.empty());

        subject.clearLocks();

        verify(jobLockRepository).clearRunningMark(JOB_TYPE);
    }

    private JobInfo jobInfo(Optional<OffsetDateTime> stopped) {
        return JobInfo.newJobInfo(JOB_ID, JOB_TYPE, this.now, this.now, stopped, JobInfo.JobStatus.OK, Collections.emptyList(), fixedClock, "HOST");
    }
}
