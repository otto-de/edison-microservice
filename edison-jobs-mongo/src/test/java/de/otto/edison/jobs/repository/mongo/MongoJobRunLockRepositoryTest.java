package de.otto.edison.jobs.repository.mongo;

import com.github.fakemongo.Fongo;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoDatabase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static com.mongodb.ErrorCategory.DUPLICATE_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.fail;

public class MongoJobRunLockRepositoryTest {

    private MongoJobRunLockRepository repo;

    @BeforeMethod
    public void setup() {
        final Fongo fongo = new Fongo("inmemory-mongodb");
        final MongoDatabase database = fongo.getDatabase("jobsinfo");
        repo = new MongoJobRunLockRepository(database);
    }

    @Test
    public void shouldStoreAndRetrieveJobInfo() {
        // given
        final OffsetDateTime now = OffsetDateTime.now();
        final JobRunLock jobRunLock = new JobRunLock("someType", now);
        final JobRunLock writtenClusterLock = repo.create(jobRunLock);
        // when
        final Optional<JobRunLock> foundLock = repo.findOne("someType");
        // then
        assertThat(foundLock.isPresent(), is(true));
        assertThat(foundLock.get(), equalTo(writtenClusterLock));
    }

    @Test
    public void shouldTakeCareThatSameLockCantBeCreatedTwice() {
        // given
        final OffsetDateTime now = OffsetDateTime.now();
        final JobRunLock jobRunLock = new JobRunLock("someType", now);
        repo.create(jobRunLock);
        // when
        try {
            repo.create(jobRunLock);
            fail( "Expected exception has not been thrown" );
        } catch (MongoWriteException mwe) {
            // then
            assertThat(mwe.getError().getCategory(), is(DUPLICATE_KEY));
        }
    }
}