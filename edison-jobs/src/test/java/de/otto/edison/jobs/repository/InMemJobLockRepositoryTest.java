package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.RunningJob;
import de.otto.edison.jobs.repository.inmem.InMemJobLockRepository;
import de.otto.edison.jobs.service.JobMutexGroups;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
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
        repository.aquireRunLock("id", "type");

        final List<RunningJob> expected = singletonList(new RunningJob("id", "type"));

        assertThat(repository.runningJobs(), is(expected));
    }

    @Test(expected = JobBlockedException.class)
    public void shouldFailToAquireLockTwice() {
        repository.aquireRunLock("id", "type");

        repository.aquireRunLock("other-id", "type");
    }

    @Test(expected = JobBlockedException.class)
    public void shouldDisableJob() {
        // given
        final String jobType = "irgendeinJobType";
        repository.disableJobType(jobType);

        // when
        try {
            repository.aquireRunLock("someId", jobType);
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
        List<String> result = repository.disabledJobTypes();

        // then
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is(jobType));
    }

    @Test
    public void shouldClearRunningJobs() throws Exception {
        //Given
        repository.aquireRunLock("someId", "type");

        //When
        repository.deleteAll();

        //Then
        assertThat(repository.runningJobs(), is(emptyList()));
    }

    @Test
    public void shouldClearDisabledJobTypes() throws Exception {
        //Given
        String jobType = "someJobType";
        repository.disableJobType(jobType);

        //When
        repository.deleteAll();

        //Then
        assertThat(repository.disabledJobTypes(), is(emptyList()));
    }

}
