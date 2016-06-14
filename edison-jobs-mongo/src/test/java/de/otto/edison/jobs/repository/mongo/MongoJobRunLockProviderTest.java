package de.otto.edison.jobs.repository.mongo;

import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteError;
import de.otto.edison.testsupport.util.TestClock;
import org.bson.BsonDocument;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class MongoJobRunLockProviderTest {

    private MongoJobRunLockProvider jobRunLockProvider;
    private MongoJobRunLockRepository jobRunLockRepository;
    private Clock clock;

    @BeforeMethod
    public void setUp() throws Exception {
        clock = TestClock.now();
        jobRunLockRepository = mock(MongoJobRunLockRepository.class);
        jobRunLockProvider = new MongoJobRunLockProvider(jobRunLockRepository, clock);

    }

    @Test
    public void shouldAcquireAllLocks() {
        // given
        Set<String> jobTypes = new HashSet<String>(Arrays.asList("jobA", "jobB"));
        final JobRunLock jobA = new JobRunLock("jobA", OffsetDateTime.now(clock));
        final JobRunLock jobB = new JobRunLock("jobB", OffsetDateTime.now(clock));
        when(jobRunLockRepository.create(jobA)).thenReturn(jobA);
        when(jobRunLockRepository.create(jobB)).thenReturn(jobB);

        // when
        final boolean runLocksForJobTypes = jobRunLockProvider.acquireRunLocksForJobTypes(jobTypes);

        // then
        assertThat(runLocksForJobTypes, is(true));
        verify(jobRunLockRepository, times(1)).create(jobA);
        verify(jobRunLockRepository, times(1)).create(jobB);
    }

    @Test
    public void shouldCanNotAcquireLockBecauseItIsAlreadyAcquired() {
        // given
        Set<String> jobTypes = new HashSet<String>(Arrays.asList("jobA"));
        final JobRunLock jobA = new JobRunLock("jobA", OffsetDateTime.now(clock));
        when(jobRunLockRepository.create(jobA))
                .thenReturn(jobA)
                .thenThrow(new MongoWriteException(new WriteError(11000,"", BsonDocument.parse("{}")), new ServerAddress()));

        // when
        final boolean runLocksForJobTypes = jobRunLockProvider.acquireRunLocksForJobTypes(jobTypes);
        final boolean runLocksForJobTypes2 = jobRunLockProvider.acquireRunLocksForJobTypes(jobTypes);

        // then
        assertThat(runLocksForJobTypes, is(true));
        assertThat(runLocksForJobTypes2, is(false));
        verify(jobRunLockRepository, times(2)).create(jobA);
    }

    @Test
    public void shouldAcquireOnlySomeLocksAndReleaseThemAgain() {
        // given
        Set<String> jobTypes1 = new HashSet<>(Arrays.asList("jobB"));
        Set<String> jobTypes2 = new HashSet<>(Arrays.asList("jobC", "jobB", "jobA"));
        final JobRunLock jobA = new JobRunLock("jobA", OffsetDateTime.now(clock));
        final JobRunLock jobB = new JobRunLock("jobB", OffsetDateTime.now(clock));
        final JobRunLock jobC = new JobRunLock("jobC", OffsetDateTime.now(clock));
        when(jobRunLockRepository.create(jobA))
                .thenReturn(jobA);
        when(jobRunLockRepository.create(jobB))
                .thenReturn(jobB)
                .thenThrow(new MongoWriteException(new WriteError(11000,"", BsonDocument.parse("{}")), new ServerAddress()));
        when(jobRunLockRepository.create(jobC))
                .thenReturn(jobA);

        // when
        final boolean runLocksForJobTypes = jobRunLockProvider.acquireRunLocksForJobTypes(jobTypes1);

        // then
        assertThat(runLocksForJobTypes, is(true));
        verify(jobRunLockRepository, times(1)).create(jobB);

        // when
        final boolean runLocksForJobTypes2 = jobRunLockProvider.acquireRunLocksForJobTypes(jobTypes2);

        // then
        assertThat(runLocksForJobTypes2, is(false));
        verify(jobRunLockRepository, times(1)).create(jobA);
        verify(jobRunLockRepository, times(2)).create(jobB);
        verify(jobRunLockRepository, times(0)).create(jobC);
        verify(jobRunLockRepository, times(1)).delete(jobA.getId());
    }


}