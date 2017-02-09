package de.otto.edison.jobs.repository.cleanup;

public class ClearDeadLocksTest {

    /*

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

        when(jobLockRepository.runningJobs()).thenReturn(asList(new RunningJob(JOB_ID, JOB_TYPE)));
        now = OffsetDateTime.now(fixedClock);
    }

    @Test
    public void shouldFindAndRemoveLockOfStoppedJob() {
        JobInfo stoppedJob = jobInfo(Optional.of(this.now));
        when(jobRepository.findOne(JOB_ID)).thenReturn(Optional.of(stoppedJob));

        subject.clearLocks();

        verify(jobLockRepository).releaseRunLock(JOB_TYPE);
    }

    @Test
    public void shouldNotClearLockOfStillRunningJob() {
        JobInfo runningJob = jobInfo(Optional.empty());
        when(jobRepository.findOne(JOB_ID)).thenReturn(Optional.of(runningJob));

        subject.clearLocks();

        verify(jobLockRepository, never()).releaseRunLock(JOB_TYPE);
    }

    @Test
    public void shouldClearLockIfNoJobInfoExists() {
        when(jobRepository.findOne(JOB_ID)).thenReturn(Optional.empty());

        subject.clearLocks();

        verify(jobLockRepository).releaseRunLock(JOB_TYPE);
    }

    private JobInfo jobInfo(Optional<OffsetDateTime> stopped) {
        return JobInfo.newJobInfo(JOB_ID, JOB_TYPE, this.now, this.now, stopped, JobInfo.JobStatus.OK, Collections.emptyList(), fixedClock, "HOST");
    }
   */
}
