package de.otto.edison.jobs.repository.cleanup;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.RunningJobs;
import de.otto.edison.jobs.repository.JobRepository;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

    private ClearDeadLocks subject;

    private Clock fixedClock;
    private OffsetDateTime now;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        subject = new ClearDeadLocks(jobRepository);

        RunningJobs jobs = new RunningJobs(asList(new RunningJobs.RunningJob(JOB_TYPE, JOB_ID)));
        when(jobRepository.runningJobsDocument()).thenReturn(jobs);
        now = OffsetDateTime.now(fixedClock);
    }

    @Test
    public void shouldFindAndRemoveLockOfStoppedJob() {
        JobInfo stoppedJob = jobInfo(Optional.of(this.now));
        when(jobRepository.findOne(JOB_TYPE)).thenReturn(Optional.of(stoppedJob));

        subject.clearLocks();

        verify(jobRepository).clearRunningMark(JOB_TYPE);
    }

    @Test
    public void shouldNotClearLockOfStillRunningJob() {
        JobInfo runningJob = jobInfo(Optional.empty());
        when(jobRepository.findOne(JOB_TYPE)).thenReturn(Optional.of(runningJob));

        subject.clearLocks();

        verify(jobRepository, never()).clearRunningMark(JOB_TYPE);
    }

    private JobInfo jobInfo(Optional<OffsetDateTime> stopped) {
        return JobInfo.newJobInfo(JOB_ID, JOB_TYPE, this.now, this.now, stopped, JobInfo.JobStatus.OK, Collections.emptyList(), fixedClock, "HOST");
    }
}