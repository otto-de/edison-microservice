package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.RunningJob;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobStateRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class JobLockServiceTest {

    @Mock
    private JobStateRepository jobStateRepository;
    @Mock
    private JobMutexGroups jobMutexGroups;

    JobLockService jobLockService;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        initMocks(this);
        when(jobMutexGroups.mutexJobTypesFor(anyString()))
                .thenReturn(emptySet());
        jobLockService = new JobLockService(jobStateRepository, jobMutexGroups);
    }

    @Test
    public void shouldAquireRunLock() throws Exception {
        when(jobStateRepository.findAllJobTypes()).thenReturn(emptySet());
        when(jobStateRepository.createValue("myJobType", "_e_running", "jobId")).thenReturn(true);

        jobLockService.aquireRunLock("jobId", "myJobType");

        verify(jobStateRepository).createValue("myJobType","_e_running", "jobId");
    }

    @Test
    public void shouldReleaseRunLock() {
        jobLockService.releaseRunLock("someType");
        verify(jobStateRepository).setValue("someType", "_e_running", null);
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotAquireLockIfAlreadyRunning() throws Exception {
        // given
        when(jobStateRepository.findAllJobTypes()).thenReturn(emptySet());

        when(jobStateRepository.createValue("myJobType", "_e_running", "someId")).thenReturn(false);

        // when
        try {
            jobLockService.aquireRunLock("jobId", "myJobType");
        }

        // then
        catch (final JobBlockedException e) {
            verify(jobStateRepository, never()).setValue(anyString(), anyString(), anyString());
            throw e;
        }
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotStartJobIfBlockedByAnotherJob() throws Exception {
        // given
        when(jobStateRepository.findAllJobTypes()).thenReturn(new HashSet<>(asList("job1", "job2")));
        when(jobStateRepository.getValue("job1", "_e_running")).thenReturn("42");
        when(jobMutexGroups.mutexJobTypesFor("job2")).thenReturn(new HashSet<>(asList("job1", "job2")));

        // when
        try {
            jobLockService.aquireRunLock("first", "job2");
        }

        // then
        catch (final JobBlockedException e) {
            verify(jobStateRepository, never()).setValue(anyString(), anyString(), anyString());
            throw e;
        }
    }

    @Test
    public void shouldReturnRunningJobsDocument() {
        when(jobStateRepository.findAllJobTypes()).thenReturn(new HashSet<>(asList("someType", "someOtherType")));
        when(jobStateRepository.getValue("someType", "_e_running")).thenReturn("someId");
        when(jobStateRepository.getValue("someOtherType", "_e_running")).thenReturn("someOtherId");

        assertThat(jobLockService.runningJobs(), containsInAnyOrder(
                new RunningJob("someId", "someType"),
                new RunningJob("someOtherId", "someOtherType")
        ));
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotStartADisabledJob() {
        // given
        when(jobStateRepository.findAllJobTypes()).thenReturn(singleton("jobType"));
        when(jobStateRepository.getValue("jobType", "_e_disabled")).thenReturn("true");

        // when
        try {
            jobLockService.aquireRunLock("someId", "jobType");
        }

        // then
        catch (JobBlockedException e) {
            assertThat(e.getMessage(), is("Job 'jobType' is currently disabled"));
            throw e;
        }
    }

    @Test
    public void shouldReturnDisabledJobTypes() {
        // given
        when(jobStateRepository.findAllJobTypes()).thenReturn(new HashSet<String>(asList("jobType", "otherJobType", "enabledJobType")));
        when(jobStateRepository.getValue("jobType", "_e_disabled")).thenReturn("true");
        when(jobStateRepository.getValue("otherJobType", "_e_disabled")).thenReturn("true");
        when(jobStateRepository.getValue("enabledJobType", "_e_disabled")).thenReturn(null);

        // when
        final Set<String> disabledJobTypes = jobLockService.disabledJobTypes();

        // then
        assertThat(disabledJobTypes, containsInAnyOrder("jobType", "otherJobType"));
    }

    @Test
    public void shouldDisableJobType() throws Exception {
        jobLockService.disableJobType("jobType");
        verify(jobStateRepository).setValue("jobType", "_e_disabled", "true");
    }

    @Test
    public void shouldEnableJobType() throws Exception {
        jobLockService.enableJobType("jobType");
        verify(jobStateRepository).setValue("jobType", "_e_disabled", null);
    }

}
