package de.otto.edison.mongo.jobs;

import com.github.fakemongo.Fongo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.jobs.domain.RunningJob;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.service.JobMutexGroup;
import de.otto.edison.jobs.service.JobMutexGroups;
import org.assertj.core.util.Lists;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class MongoJobLockRepositoryTest {

    private MongoJobLockRepository repo;
    private MongoCollection<Document> runningJobsCollection;

    @Before
    public void setup() {
        final Fongo fongo = new Fongo("inmemory-mongodb");
        final MongoDatabase database = fongo.getDatabase("jobsinfo");
        runningJobsCollection = database.getCollection("jobmetadata");
        JobMutexGroups jobMutexGroups = new JobMutexGroups();
        jobMutexGroups.setMutexGroups(singleton(new JobMutexGroup("testgroup", "FirstMutexJob", "OtherMutexJob")));
        repo = new MongoJobLockRepository(database, jobMutexGroups);
        repo.init();
    }

    @Test
    public void shouldInitMetaDataCollection() {
        assertThat(runningJobsCollection.count(new Document("_id", "RUNNING_JOBS")), is(1L));
        assertThat(runningJobsCollection.count(new Document("_id", "DISABLED_JOBS")), is(1L));
    }

    @Test
    public void shouldMarkJobAsRunning() throws Exception {
        final String jobType = "myJobType";
        String jobId = "jobId";

        repo.aquireRunLock(jobId, jobType);

        assertRunningDocumentContainsJob(jobType, jobId);
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotStartJobIfAlreadyRunning() throws Exception {
        // given
        final String jobType = "myJobType";
        final String jobId = "jobId";
        addJobToRunningDocument(jobType, jobId);

        // when
        try {
            repo.aquireRunLock(jobId, jobType);
        }

        // then
        catch (JobBlockedException e) {
            assertRunningDocumentContainsJob(jobType, jobId);
            throw e;
        }
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotStartJobIfBlockedByAnotherJob() throws Exception {
        // given
        final String jobType = "FirstMutexJob";
        final String otherJobType = "OtherMutexJob";
        addJobToRunningDocument(otherJobType, "first");

        // when
        try {
            repo.aquireRunLock("first", jobType);
        }

        // then
        catch (JobBlockedException e) {
            assertRunningDocumentContainsJob(otherJobType, "first");
            throw e;
        }
    }

    @Test
    public void shouldRemoveStoppedJobFromRunningDocument() {
        String jobType = "myJobType";
        addJobToRunningDocument("otherJobType", "other");
        addJobToRunningDocument(jobType, "this");

        repo.releaseRunLock(jobType);

        assertRunningDocumentNotContainsJob(jobType);
        assertRunningDocumentContainsJob("otherJobType", "other");
    }

    @Test
    public void shouldReturnRunningJobsDocument() {
        repo.aquireRunLock("id", "type");

        List<RunningJob> expected = Collections.singletonList(new RunningJob("id", "type"));

        assertThat(repo.runningJobs(), is(expected));
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotStartADisabledJob() {
        // given
        String jobType = "irgendeinJobType";
        repo.disableJobType(jobType);

        // when
        try {
            repo.aquireRunLock("someId", jobType);
        }

        // then
        catch (JobBlockedException e) {
            assertThat(e.getMessage(), is("Disabled"));
            throw e;
        }
    }

    @Test
    public void shouldStartAnEnabledJob() {
        // given
        String jobType = "irgendeinJobType";
        repo.disableJobType(jobType);
        repo.enableJobType(jobType);

        // when
        repo.aquireRunLock("someId", "jobType");

        // then
        List<RunningJob> runningJobs = repo.runningJobs();
        assertThat(runningJobs, hasSize(1));
        assertThat(runningJobs.get(0).jobType, is("jobType"));
        assertThat(runningJobs.get(0).jobId, is("someId"));
    }

    @Test
    public void shouldFindDisabledJobTypes() {
        // given
        String jobType = "irgendeinJobType";
        repo.disableJobType(jobType);

        // when
        List<String> result = repo.disabledJobTypes();

        // then
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is(jobType));
    }

    @Test
    public void shouldClearDisabledJobTypes() throws Exception {
        // given
        String jobType = "someJobType";
        repo.disableJobType(jobType);

        // when
        repo.deleteAll();

        // then
        assertThat(repo.disabledJobTypes(), is(Lists.emptyList()));
        assertThat(runningJobsCollection.count(new Document("_id", "DISABLED_JOBS")), is(1L));
    }

    @Test
    public void shouldClearRunningJobs() throws Exception {
        // given
        repo.aquireRunLock("id", "type");

        // when
        repo.deleteAll();

        // then
        assertThat(repo.runningJobs(), is(Lists.emptyList()));
        assertThat(runningJobsCollection.count(new Document("_id", "RUNNING_JOBS")), is(1L));
    }

    private void addJobToRunningDocument(String jobType, String jobId) {
        runningJobsCollection.updateOne(
                new Document("_id", "RUNNING_JOBS"),
                new Document("$set",
                        new Document(jobType, jobId))
        );
    }


    private void assertRunningDocumentContainsJob(String jobType, String jobId) {
        Document runningJobsDocument = runningJobsCollection.find(new Document("_id", "RUNNING_JOBS")).iterator().next();
        assertThat(runningJobsDocument.entrySet(), is(new HashMap<String, Object>() {{
            put("_id", "RUNNING_JOBS");
            put(jobType, jobId);
        }}.entrySet()));
    }

    private void assertRunningDocumentNotContainsJob(String jobType) {
        Document runningJobsDocument = runningJobsCollection.find(new Document("_id", "RUNNING_JOBS")).iterator().next();
        assertThat(runningJobsDocument.containsKey(jobType), is(false));
    }

}
