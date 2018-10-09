package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.domain.RunningJob;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobMetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class JobMetaServiceTest {

    @Mock
    private JobMetaRepository jobMetaRepository;
    @Mock
    private JobMutexGroups jobMutexGroups;

    private JobMetaService jobMetaService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        initMocks(this);
        when(jobMutexGroups.mutexJobTypesFor(anyString()))
                .thenReturn(emptySet());
        jobMetaService = new JobMetaService(jobMetaRepository, jobMutexGroups);
    }

    @Test
    public void shouldAquireRunLock() throws Exception {
        when(jobMetaRepository.findAllJobTypes()).thenReturn(emptySet());
        when(jobMetaRepository.setRunningJob("myJobType", "jobId")).thenReturn(true);
        when(jobMetaRepository.getJobMeta("myJobType")).thenReturn(new JobMeta("myJobType", false, false, "", emptyMap()));

        jobMetaService.aquireRunLock("jobId", "myJobType");

        verify(jobMetaRepository).setRunningJob("myJobType", "jobId");
    }

    @Test
    public void shouldReleaseRunLock() {
        jobMetaService.releaseRunLock("someType");
        verify(jobMetaRepository).clearRunningJob("someType");
    }

    @Test
    public void shouldNotAquireLockIfAlreadyRunning() throws Exception {
        // given
        when(jobMetaRepository.findAllJobTypes()).thenReturn(emptySet());
        when(jobMetaRepository.getJobMeta("myJobType")).thenReturn(new JobMeta("myJobType", true, false, "", emptyMap()));
        when(jobMetaRepository.setRunningJob("myJobType", "someId")).thenReturn(false);

        // when
        assertThrows(JobBlockedException.class, () -> jobMetaService.aquireRunLock("jobId", "myJobType"));
    }

    @Test
    public void shouldNotStartJobIfBlockedByAnotherJob() throws Exception {
        // given
        when(jobMetaRepository.findAllJobTypes()).thenReturn(new HashSet<>(asList("job1", "job2")));
        when(jobMetaRepository.getRunningJob("job1")).thenReturn("42");
        when(jobMetaRepository.getJobMeta("job2")).thenReturn(new JobMeta("job2", false, false, "", emptyMap()));
        when(jobMetaRepository.setRunningJob("job2", "first")).thenReturn(true);
        when(jobMutexGroups.mutexJobTypesFor("job2")).thenReturn(new HashSet<>(asList("job1", "job2")));

        // when

        // then
        assertThrows(JobBlockedException.class, () -> jobMetaService.aquireRunLock("first", "job2"));
        verify(jobMetaRepository).clearRunningJob("job2");
    }

    @Test
    public void shouldReturnRunningJobsDocument() {
        when(jobMetaRepository.findAllJobTypes()).thenReturn(new HashSet<>(asList("someType", "someOtherType")));
        when(jobMetaRepository.getRunningJob("someType")).thenReturn("someId");
        when(jobMetaRepository.getRunningJob("someOtherType")).thenReturn("someOtherId");

        assertThat(jobMetaService.runningJobs(), containsInAnyOrder(
                new RunningJob("someId", "someType"),
                new RunningJob("someOtherId", "someOtherType")
        ));
    }

    @Test
    public void shouldNotStartADisabledJob() {
        // given
        when(jobMetaRepository.findAllJobTypes()).thenReturn(singleton("jobType"));
        when(jobMetaRepository.getJobMeta("jobType")).thenReturn(
                new JobMeta("jobType", false, true, "", emptyMap())
        );

        // when

        // then
        assertThrows(JobBlockedException.class, () -> jobMetaService.aquireRunLock("someId", "jobType"), "Job 'jobType' is currently disabled");
    }

    @Test
    public void shouldDisableJobType() throws Exception {
        jobMetaService.disable("jobType", null);
        verify(jobMetaRepository).disable("jobType", null);
    }

    @Test
    public void shouldDisableJobTypeWithComment() throws Exception {
        jobMetaService.disable("jobType", "some comment");
        verify(jobMetaRepository).disable("jobType", "some comment");
    }

    @Test
    public void shouldEnableJobType() throws Exception {
        jobMetaService.enable("jobType");
        verify(jobMetaRepository).enable("jobType");
    }

}
