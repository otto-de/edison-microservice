package de.otto.edison.mongo.jobs;

import com.github.fakemongo.Fongo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.domain.RunningJobs;
import de.otto.edison.jobs.repository.JobBlockedException;
import org.assertj.core.util.Lists;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.*;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.testsupport.util.Sets.hashSet;
import static java.time.Clock.systemDefaultZone;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
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
        repo = new MongoJobLockRepository(database);
        repo.initJobsMetaDataDocumentsOnStartup();
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
        JobInfo jobInfo = jobInfo(jobId, jobType);

        repo.markJobAsRunningIfPossible(jobInfo, hashSet(jobType));

        assertRunningDocumentContainsJob(jobType, jobId);
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotStartJobIfAlreadyRunning() throws Exception {
        // given
        final String jobType = "myJobType";
        final String jobId = "jobId";
        addJobToRunningDocument(jobType);

        // when
        try {
            repo.markJobAsRunningIfPossible(jobInfo(jobId, jobType), hashSet(jobType));
        }

        // then
        catch (JobBlockedException e) {
            assertRunningDocumentContainsJob(jobType, jobType);
            throw e;
        }
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotStartJobIfBlockedByAnotherJob() throws Exception {
        // given
        final String jobType = "myJobType";
        final String otherJobType = "myOtherJobType";
        addJobToRunningDocument(otherJobType);

        // when
        try {
            repo.markJobAsRunningIfPossible(jobInfo("", jobType), hashSet(jobType, otherJobType));
        }

        // then
        catch (JobBlockedException e) {
            assertRunningDocumentContainsJob(otherJobType, otherJobType);
            throw e;
        }
    }

    @Test
    public void shouldRemoveStoppedJobFromRunningDocument() {
        String jobType = "myJobType";
        addJobToRunningDocument("otherJobType");
        addJobToRunningDocument(jobType);

        repo.clearRunningMark(jobType);

        assertRunningDocumentNotContainsJob(jobType);
        assertRunningDocumentContainsJob("otherJobType", "otherJobType");
    }

    @Test
    public void shouldReturnRunningJobsDocument() {
        repo.markJobAsRunningIfPossible(someRunningJobInfo("id", "type", now()), new HashSet<>());

        RunningJobs expected = new RunningJobs(Collections.singletonList(new RunningJobs.RunningJob("id", "type")));

        assertThat(repo.runningJobs(), is(expected));
    }

    @Test(expected = JobBlockedException.class)
    public void shouldNotStartADisabledJob() {
        // given
        String jobType = "irgendeinJobType";
        repo.disableJobType(jobType);
        JobInfo jobInfo = JobInfo.newJobInfo("someId", jobType, systemDefaultZone(), "lokalhorst");

        // when
        try {
            repo.markJobAsRunningIfPossible(jobInfo, new HashSet<>());
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
        JobInfo jobInfo = JobInfo.newJobInfo("someId", jobType, systemDefaultZone(), "lokalhorst");

        // when
        repo.markJobAsRunningIfPossible(jobInfo, new HashSet<>());

        // then
        List<RunningJobs.RunningJob> runningJobs = repo.runningJobs().getRunningJobs();
        assertThat(runningJobs, hasSize(1));
        assertThat(runningJobs.get(0).jobType, is(jobType));
    }

    @Test
    public void shouldFindDisabledJobTypes() {
        // given
        String jobType = "irgendeinJobType";
        repo.disableJobType(jobType);

        // when
        List<String> result = repo.findDisabledJobTypes();

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
        assertThat(repo.findDisabledJobTypes(), is(Lists.emptyList()));
        assertThat(runningJobsCollection.count(new Document("_id", "DISABLED_JOBS")), is(1L));
    }

    @Test
    public void shouldClearRunningJobs() throws Exception {
        // given
        repo.markJobAsRunningIfPossible(someRunningJobInfo("id", "type", now()), new HashSet<>());

        // when
        repo.deleteAll();

        // then
        assertThat(repo.runningJobs(), is(new RunningJobs(Lists.emptyList())));
        assertThat(runningJobsCollection.count(new Document("_id", "RUNNING_JOBS")), is(1L));
    }

    private void addJobToRunningDocument(String jobType) {
        runningJobsCollection.updateOne(
                new Document("_id", "RUNNING_JOBS"),
                new Document("$set",
                        new Document(jobType, jobType))
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

    private JobInfo jobInfo(final String jobId, final String type) {
        return JobInfo.newJobInfo(
                jobId,
                type,
                now(), now(), Optional.of(now()), OK,
                asList(
                        jobMessage(Level.INFO, "foo", now()),
                        jobMessage(Level.WARNING, "bar", now())),
                systemDefaultZone(),
                "localhost"
        );
    }

    private JobInfo someRunningJobInfo(final String jobId, final String type, final OffsetDateTime started) {
        return JobInfo.newJobInfo(
                jobId,
                type,
                started, started.plus(1, SECONDS), Optional.empty(), OK,
                Collections.emptyList(),
                systemDefaultZone(),
                "localhost"
        );
    }
}
