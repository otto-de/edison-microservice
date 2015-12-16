package de.otto.edison.jobs.repository.mongo;

import com.github.fakemongo.Fongo;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import org.bson.Document;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.repository.mongo.JobStructure.*;
import static java.time.Clock.systemDefaultZone;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MongoJobRepositoryTest {

    private MongoJobRepository repo;

    @BeforeMethod
    public void setup() {
        final Fongo fongo = new Fongo("inmemory-mongodb");
        final MongoDatabase database = fongo.getDatabase("jobsinfo");
        repo = new MongoJobRepository(database, systemDefaultZone());
    }

    @Test
    public void shouldStoreAndRetrieveJobInfo() {
        // given
        final JobInfo foo = someJobInfo("http://localhost/foo/A");
        final JobInfo writtenFoo = repo.create(foo);
        // when
        final Optional<JobInfo> jobInfo = repo.findOne(URI.create("http://localhost/foo/A"));
        // then
        assertThat(jobInfo.isPresent(), is(true));
        assertThat(jobInfo.get(), is(writtenFoo));
    }

    @Test
    public void shouldUpdateJobInfo() {
        // given
        final JobInfo foo = someJobInfo("http://localhost/foo/B");
        repo.createOrUpdate(foo);
        final JobInfo writtenFoo = repo.createOrUpdate(foo.info("some message"));
        // when
        final Optional<JobInfo> jobInfo = repo.findOne(URI.create("http://localhost/foo/B"));
        // then
        assertThat(jobInfo.get(), is(writtenFoo));
    }

    @Test
    public void shouldStoreAndRetrieveRunningJobInfo() {
        // given
        final JobInfo foo = someRunningJobInfo("http://localhost/foo", "SOME_JOB", now());
        repo.createOrUpdate(foo);
        // when
        final Optional<JobInfo> jobInfo = repo.findOne(URI.create("http://localhost/foo"));
        // then
        assertThat(jobInfo.get(), is(foo));
    }

    @Test
    public void shouldStoreAndRetrieveAllJobInfo() {
        // given
        repo.createOrUpdate(someJobInfo("http://localhost/foo"));
        repo.createOrUpdate(someJobInfo("http://localhost/bar"));
        // when
        final List<JobInfo> jobInfos = repo.findAll();
        // then
        assertThat(jobInfos, hasSize(2));
    }

    @Test
    public void shouldFindLatest() {
        // given
        repo.createOrUpdate(someRunningJobInfo("http://localhost/foo", "SOME_JOB", now()));
        JobInfo later = someRunningJobInfo("http://localhost/bar", "SOME_JOB", now().plus(1, SECONDS));
        repo.createOrUpdate(later);
        JobInfo evenLater = someRunningJobInfo("http://localhost/foobar", "SOME_JOB", now().plus(2, SECONDS));
        repo.createOrUpdate(evenLater);
        // when
        final List<JobInfo> jobInfos = repo.findLatest(2);
        // then
        assertThat(jobInfos, hasSize(2));
        assertThat(jobInfos, containsInAnyOrder(later, evenLater));
    }

    @Test
    public void shouldFindLatestByType() {
        // given
        final JobInfo oldest = someRunningJobInfo("http://localhost/foo", "SOME_JOB", now());
        final JobInfo later = someRunningJobInfo("http://localhost/bar", "SOME_OTHER_JOB", now().plus(1, SECONDS));
        final JobInfo evenLater = someRunningJobInfo("http://localhost/foobar", "SOME_JOB", now().plus(2, SECONDS));
        repo.createOrUpdate(oldest);
        repo.createOrUpdate(later);
        repo.createOrUpdate(evenLater);
        // when
        final List<JobInfo> jobInfos = repo.findLatestBy("SOME_JOB", 2);
        // then
        assertThat(jobInfos, containsInAnyOrder(oldest, evenLater));
    }

    @Test
    public void shouldFindByType() {
        // given
        final JobInfo foo = someJobInfo("http://localhost/foo", "T_FOO");
        repo.createOrUpdate(foo);
        repo.createOrUpdate(someJobInfo("http://localhost/bar", "T_BAR"));
        // when
        final List<JobInfo> jobInfos = repo.findByType("T_FOO");
        // then
        assertThat(jobInfos, contains(foo));
    }

    @Test
    public void shouldFindRunningByType() {
        // given
        final JobInfo foo = someJobInfo("http://localhost/foo", "T_FOO");
        final JobInfo bar = someRunningJobInfo("http://localhost/bar", "T_BAR", now());
        repo.createOrUpdate(foo);
        repo.createOrUpdate(bar);
        // when
        final Optional<JobInfo> optionalFoo = repo.findRunningJobByType("T_FOO");
        final Optional<JobInfo> optionalBar = repo.findRunningJobByType("T_BAR");
        // then
        assertThat(optionalFoo.isPresent(), is(false));
        assertThat(optionalBar.isPresent(), is(true));
    }

    @Test
    public void shouldFindRunningWithoutUpdateSince() {
        // given
        final JobInfo foo = someJobInfo("http://localhost/foo", "T_FOO");
        final JobInfo bar = someRunningJobInfo("http://localhost/bar", "T_BAR", now());
        final JobInfo foobar = someRunningJobInfo("http://localhost/foobar", "T_BAR", now().plusSeconds(3));
        repo.createOrUpdate(foo);
        repo.createOrUpdate(bar);
        repo.createOrUpdate(foobar);
        // when
        final List<JobInfo> infos = repo.findRunningWithoutUpdateSince(now().plusSeconds(2));
        // then
        assertThat(infos, hasSize(1));
        assertThat(infos.get(0), is(bar));
    }

    @Test
    public void shouldRemoveIfStopped() {
        // given
        final JobInfo foo = someJobInfo("http://localhost/foo", "T_FOO");
        final JobInfo bar = someRunningJobInfo("http://localhost/bar", "T_BAR", now());
        repo.createOrUpdate(foo);
        repo.createOrUpdate(bar);
        // when
        repo.removeIfStopped(URI.create("http://localhost/foo"));
        repo.removeIfStopped(URI.create("http://localhost/bar"));
        // then
        assertThat(repo.findAll(), hasSize(1));
        assertThat(repo.findAll(), contains(bar));
    }

    @Test
    public void shouldNotFailInCaseLogMessageHasNoText() throws Exception {
        //given
        Map<String, Object> infoLog = new HashMap<String, Object>() {{
            put(MSG_LEVEL.key(), "INFO");
            put(MSG_TEXT.key(), "Some text");
            put(MSG_TS.key(), new Date());
        }};

        Map<String, Object> errorLog = new HashMap<String, Object>() {{
            put(MSG_LEVEL.key(), "ERROR");
            put(MSG_TEXT.key(), null);
            put(MSG_TS.key(), new Date());
        }};

        Document infoLogDocument = new Document(infoLog);
        Document errorLogDocument = new Document(errorLog);

        Map<String, Object> jobLogs = new HashMap<String, Object>() {{
            put(MESSAGES.key(), asList(infoLogDocument, errorLogDocument));
            put(JOB_TYPE.key(), "SomeType");
            put(ID.key(), "/SomeType/ID");
            put(STATUS.key(), JobStatus.ERROR.toString());
        }};

        //when
        JobInfo jobInfo = repo.decode(new Document(jobLogs));

        //then
        assertThat(jobInfo.getMessages().size(), is(2));
    }

    @Test
    public void shouldFindStatusOfAJob() throws Exception {
        //Given
        final JobInfo foo = someJobInfo("http://localhost/foo", "T_FOO");
        repo.createOrUpdate(foo);

        //When
        JobStatus status = repo.findStatus(URI.create("http://localhost/foo"));

        //Then
        assertThat(status, is(OK));
    }

    private JobInfo someJobInfo(final String jobUri) {
        return JobInfo.newJobInfo(
                URI.create(jobUri),
                "SOME_JOB",
                now(), now(), Optional.of(now()), OK,
                asList(
                        jobMessage(Level.INFO, "foo"),
                        jobMessage(Level.WARNING, "bar")),
                systemDefaultZone()
        );
    }

    private JobInfo someJobInfo(final String jobUri, final String type) {
        return JobInfo.newJobInfo(
                URI.create(jobUri),
                type,
                now(), now(), Optional.of(now()), OK,
                asList(
                        jobMessage(Level.INFO, "foo"),
                        jobMessage(Level.WARNING, "bar")),
                systemDefaultZone()
        );
    }

    private JobInfo someRunningJobInfo(final String jobUri, final String type, final OffsetDateTime started) {
        return JobInfo.newJobInfo(
                URI.create(jobUri),
                type,
                started, started.plus(1, SECONDS), Optional.empty(), OK,
                Collections.<JobMessage>emptyList(),
                systemDefaultZone()
        );
    }

    @Test
    public void shouldFindAll() {

    }
}