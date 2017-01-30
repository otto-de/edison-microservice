package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.RunningJobs;
import de.otto.edison.jobs.repository.inmem.InMemJobLockRepository;
import de.otto.edison.jobs.service.JobMutexGroups;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.time.Clock.systemDefaultZone;
import static java.util.Collections.emptySet;
import static org.assertj.core.util.Lists.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class InMemJobLockRepositoryTest {

    private InMemJobLockRepository repository;

    @Before
    public void setUp() throws Exception {
        final JobMutexGroups mutexGroups = new JobMutexGroups(emptySet());
        repository = new InMemJobLockRepository(mutexGroups);
    }

    @Test
    public void shouldReturnRunningJobsDocument() {
        repository.markJobAsRunningIfPossible(newJobInfo("id", "type", systemDefaultZone(), "host"));

        RunningJobs expected = new RunningJobs(Collections.singletonList(new RunningJobs.RunningJob("id", "type")));

        assertThat(repository.runningJobs(), is(expected));
    }

    @Test(expected = JobBlockedException.class)
    public void shouldFailToAquireLockTwice() {
        repository.markJobAsRunningIfPossible(newJobInfo("id", "type", systemDefaultZone(), "host"));

        repository.markJobAsRunningIfPossible(newJobInfo("other-id", "type", systemDefaultZone(), "host"));
    }

    @Test(expected = JobBlockedException.class)
    public void shouldDisableJob() {
        // given
        String jobType = "irgendeinJobType";
        repository.disableJobType(jobType);
        JobInfo jobInfo = JobInfo.newJobInfo("someId", jobType, systemDefaultZone(), "lokalhorst");

        // when
        try {
            repository.markJobAsRunningIfPossible(jobInfo);
        }

        // then
        catch(JobBlockedException e) {
            assertThat(e.getMessage(), is("Disabled"));
            throw e;
        }
    }

    @Test
    public void shouldFindDisabledJobTypes() {
        // given
        String jobType = "irgendeinJobType";
        repository.disableJobType(jobType);

        // when
        List<String> result = repository.findDisabledJobTypes();

        // then
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is(jobType));
    }

    @Test
    public void shouldClearRunningJobs() throws Exception {
        //Given
        repository.markJobAsRunningIfPossible(newJobInfo("id", "type", systemDefaultZone(), "host"));

        //When
        repository.deleteAll();

        //Then
        assertThat(repository.runningJobs(), is(new RunningJobs(emptyList())));
    }

    @Test
    public void shouldClearDisabledJobTypes() throws Exception {
        //Given
        String jobType = "someJobType";
        repository.disableJobType(jobType);

        //When
        repository.deleteAll();

        //Then
        assertThat(repository.findDisabledJobTypes(), is(emptyList()));
    }

}
