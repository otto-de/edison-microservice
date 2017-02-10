package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.DisabledJob;
import de.otto.edison.jobs.domain.RunningJob;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobMetaRepository;
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

public class JobMetaServiceTest {

    @Mock
    private JobMetaRepository jobMetaRepository;
    @Mock
    private JobMutexGroups jobMutexGroups;

    JobMetaService jobMetaService;

    @Before
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
        when(jobMetaRepository.createValue("myJobType", "_e_running", "jobId")).thenReturn(true);

        jobMetaService.aquireRunLock("jobId", "myJobType");

        verify(jobMetaRepository).createValue("myJobType","_e_running", "jobId");
    }

    @Test
    public void shouldReleaseRunLock() {
        jobMetaService.releaseRunLock("someType");
        verify(jobMetaRepository).setValue("someType", "_e_running", null);
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotAquireLockIfAlreadyRunning() throws Exception {
        // given
        when(jobMetaRepository.findAllJobTypes()).thenReturn(emptySet());

        when(jobMetaRepository.createValue("myJobType", "_e_running", "someId")).thenReturn(false);

        // when
        try {
            jobMetaService.aquireRunLock("jobId", "myJobType");
        }

        // then
        catch (final JobBlockedException e) {
            verify(jobMetaRepository, never()).setValue(anyString(), anyString(), anyString());
            throw e;
        }
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotStartJobIfBlockedByAnotherJob() throws Exception {
        // given
        when(jobMetaRepository.findAllJobTypes()).thenReturn(new HashSet<>(asList("job1", "job2")));
        when(jobMetaRepository.getValue("job1", "_e_running")).thenReturn("42");
        when(jobMutexGroups.mutexJobTypesFor("job2")).thenReturn(new HashSet<>(asList("job1", "job2")));

        // when
        try {
            jobMetaService.aquireRunLock("first", "job2");
        }

        // then
        catch (final JobBlockedException e) {
            verify(jobMetaRepository, never()).setValue(anyString(), anyString(), anyString());
            throw e;
        }
    }

    @Test
    public void shouldReturnRunningJobsDocument() {
        when(jobMetaRepository.findAllJobTypes()).thenReturn(new HashSet<>(asList("someType", "someOtherType")));
        when(jobMetaRepository.getValue("someType", "_e_running")).thenReturn("someId");
        when(jobMetaRepository.getValue("someOtherType", "_e_running")).thenReturn("someOtherId");

        assertThat(jobMetaService.runningJobs(), containsInAnyOrder(
                new RunningJob("someId", "someType"),
                new RunningJob("someOtherId", "someOtherType")
        ));
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotStartADisabledJob() {
        // given
        when(jobMetaRepository.findAllJobTypes()).thenReturn(singleton("jobType"));
        when(jobMetaRepository.getValue("jobType", "_e_disabled")).thenReturn("true");

        // when
        try {
            jobMetaService.aquireRunLock("someId", "jobType");
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
        when(jobMetaRepository.findAllJobTypes()).thenReturn(new HashSet<String>(asList("jobType", "otherJobType", "enabledJobType")));
        when(jobMetaRepository.getValue("jobType", "_e_disabled")).thenReturn("some comment");
        when(jobMetaRepository.getValue("otherJobType", "_e_disabled")).thenReturn("");
        when(jobMetaRepository.getValue("enabledJobType", "_e_disabled")).thenReturn(null);

        // when
        final Set<DisabledJob> disabledJobTypes = jobMetaService.disabledJobTypes();

        // then
        assertThat(disabledJobTypes, containsInAnyOrder(
                new DisabledJob("jobType", "some comment"),
                new DisabledJob("otherJobType", "")));
    }

    @Test
    public void shouldDisableJobType() throws Exception {
        jobMetaService.disableJobType(new DisabledJob("jobType", null));
        verify(jobMetaRepository).setValue("jobType", "_e_disabled", "");
    }

    @Test
    public void shouldDisableJobTypeWithComment() throws Exception {
        jobMetaService.disableJobType(new DisabledJob("jobType", "some comment"));
        verify(jobMetaRepository).setValue("jobType", "_e_disabled", "some comment");
    }

    @Test
    public void shouldEnableJobType() throws Exception {
        jobMetaService.enableJobType("jobType");
        verify(jobMetaRepository).setValue("jobType", "_e_disabled", null);
    }

}
