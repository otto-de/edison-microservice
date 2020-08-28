package de.otto.edison.jobs.repository.mongo;

import ch.qos.logback.classic.Logger;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.mongo.configuration.MongoProperties;
import de.otto.edison.testsupport.matcher.OptionalMatchers;
import org.assertj.core.util.Lists;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MongoDBContainer;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.ERROR;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.repository.mongo.JobStructure.*;
import static java.time.Clock.systemDefaultZone;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MongoJobRepositoryTest {

    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2.5");

    @BeforeAll
    public static void startMongo() {
        mongoDBContainer.start();
    }

    @AfterAll
    public static void teardownMongo() {
        mongoDBContainer.stop();
    }

    private MongoJobRepository repo;

    @BeforeEach
    public void setup() {
        final MongoDatabase mongoDatabase = MongoClients.create(mongoDBContainer.getReplicaSetUrl()).getDatabase("jobsinfo-" + UUID.randomUUID());
        repo = new MongoJobRepository(mongoDatabase, "jobsinfo", new MongoProperties());
    }

    @Test
    public void shouldStoreAndRetrieveJobInfo() {
        // given
        final JobInfo foo = someJobInfo("http://localhost/foo/A");
        final JobInfo writtenFoo = repo.create(foo);
        // when
        final Optional<JobInfo> jobInfo = repo.findOne("http://localhost/foo/A");
        // then
        assertThat(jobInfo.isPresent(), is(true));
        assertThat(jobInfo.orElse(null), is(writtenFoo));
    }

    @Test
    public void shouldUpdateJobInfo() {
        // given
        final JobInfo foo = someJobInfo("http://localhost/foo/B");
        repo.createOrUpdate(foo);
        final JobInfo writtenFoo = repo.createOrUpdate(foo.copy().addMessage(jobMessage(Level.INFO, "some message", now(foo.getClock()))).build());
        // when
        final Optional<JobInfo> jobInfo = repo.findOne("http://localhost/foo/B");
        // then
        assertThat(jobInfo.orElse(null), is(writtenFoo));
    }

    @Test
    public void shouldUpdateJobStatus() {
        //Given
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO"); //default jobStatus is 'OK'
        repo.createOrUpdate(foo);

        //When
        repo.setJobStatus(foo.getJobId(), ERROR);
        final JobStatus status = repo.findStatus("http://localhost/foo");

        //Then
        assertThat(status, is(ERROR));
    }

    @Test
    public void shouldUpdateJobLastUpdateTime() {
        //Given
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO");
        repo.createOrUpdate(foo);

        final OffsetDateTime myTestTime = OffsetDateTime.of(1979, 2, 5, 1, 2, 3, 4, ZoneOffset.UTC);

        //When
        repo.setLastUpdate(foo.getJobId(), myTestTime);

        final Optional<JobInfo> jobInfo = repo.findOne(foo.getJobId());

        //Then
        assertThat(jobInfo, OptionalMatchers.isPresent());
        assertThat(Date.from(jobInfo.get().getLastUpdated().toInstant()), is(Date.from(myTestTime.toInstant())));
    }

    @Test
    public void shouldStoreAndRetrieveRunningJobInfo() {
        // given
        final JobInfo foo = someRunningJobInfo("http://localhost/foo", "SOME_JOB", now());
        repo.createOrUpdate(foo);
        // when
        final Optional<JobInfo> jobInfo = repo.findOne("http://localhost/foo");
        // then
        assertThat(jobInfo.orElse(null), is(foo));
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
    public void shouldStoreAndRetrieveAllJobInfoWithoutMessages() {
        // given
        final JobInfo job1 = someJobInfo("http://localhost/foo");
        final JobInfo job2 = someJobInfo("http://localhost/bar");

        repo.createOrUpdate(job1);
        repo.createOrUpdate(job2);
        // when
        final List<JobInfo> jobInfos = repo.findAllJobInfoWithoutMessages();
        // then
        assertThat(jobInfos, hasSize(2));
        assertThat(jobInfos.get(0), is(job1.copy().setMessages(emptyList()).build()));
        assertThat(jobInfos.get(1), is(job2.copy().setMessages(emptyList()).build()));
    }

    @Test
    public void shouldFindLatest() {
        // given
        repo.createOrUpdate(someRunningJobInfo("http://localhost/foo", "SOME_JOB", now()));
        final JobInfo later = someRunningJobInfo("http://localhost/bar", "SOME_JOB", now().plus(1, SECONDS));
        repo.createOrUpdate(later);
        final JobInfo evenLater = someRunningJobInfo("http://localhost/foobar", "SOME_JOB", now().plus(2, SECONDS));
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
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO");
        repo.createOrUpdate(foo);
        repo.createOrUpdate(jobInfo("http://localhost/bar", "T_BAR"));
        // when
        final List<JobInfo> jobInfos = repo.findByType("T_FOO");
        // then
        assertThat(jobInfos, contains(foo));
    }

    @Test
    public void shouldFindRunningWithoutUpdateSince() {
        // given
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO");
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
        final JobInfo foo = jobInfo("foo", "T_FOO");
        final JobInfo bar = someRunningJobInfo("bar", "T_BAR", now());
        repo.createOrUpdate(foo);
        repo.createOrUpdate(bar);
        // when
        repo.removeIfStopped("foo");
        repo.removeIfStopped("bar");
        // then
        assertThat(repo.findAll(), hasSize(1));
        assertThat(repo.findAll(), contains(bar));
    }

    @Test
    public void shouldFindAllJobTypes() throws Exception {
        // Given
        final OffsetDateTime now = OffsetDateTime.now();
        final JobInfo eins = someRunningJobInfo("jobEins", "someJobTypeEins", now);
        final JobInfo zwei = someRunningJobInfo("jobZwei", "someJobTypeZwei", now.plusSeconds(1));
        final JobInfo drei = someRunningJobInfo("jobDrei", "someJobTypeDrei", now.plusSeconds(2));
        final JobInfo vierWithTypeDrei = someRunningJobInfo("jobVier", "someJobTypeDrei", now.plusSeconds(3));

        repo.createOrUpdate(eins);
        repo.createOrUpdate(zwei);
        repo.createOrUpdate(drei);
        repo.createOrUpdate(vierWithTypeDrei);

        // When
        final List<String> allJobIds = repo.findAllJobIdsDistinct();

        // Then
        assertThat(allJobIds, hasSize(3));
        assertThat(allJobIds, containsInAnyOrder("jobEins", "jobZwei", "jobVier"));
    }

    @Test
    public void shouldFindLatestDistinct() throws Exception {
        // Given
        final OffsetDateTime now = OffsetDateTime.now();
        final JobInfo eins = someRunningJobInfo("http://localhost/eins", "someJobType", now);
        final JobInfo zwei = someRunningJobInfo("http://localhost/zwei", "someOtherJobType", now.plusSeconds(1));
        final JobInfo drei = someRunningJobInfo("http://localhost/drei", "nextJobType", now.plusSeconds(2));
        final JobInfo vier = someRunningJobInfo("http://localhost/vier", "someJobType", now.plusSeconds(3));
        final JobInfo fuenf = someRunningJobInfo("http://localhost/fuenf", "someJobType", now.plusSeconds(4));

        repo.createOrUpdate(eins);
        repo.createOrUpdate(zwei);
        repo.createOrUpdate(drei);
        repo.createOrUpdate(vier);
        repo.createOrUpdate(fuenf);

        // When
        final List<JobInfo> latestDistinct = repo.findLatestJobsDistinct();

        // Then
        assertThat(latestDistinct, hasSize(3));
        assertThat(latestDistinct, containsInAnyOrder(fuenf, zwei, drei));
    }

    @Test
    public void shouldNotFailInCaseLogMessageHasNoText() throws Exception {
        //given
        final Map<String, Object> infoLog = new HashMap<String, Object>() {{
            put(MSG_LEVEL.key(), "INFO");
            put(MSG_TEXT.key(), "Some text");
            put(MSG_TS.key(), new Date());
        }};

        final Map<String, Object> errorLog = new HashMap<String, Object>() {{
            put(MSG_LEVEL.key(), "ERROR");
            put(MSG_TEXT.key(), null);
            put(MSG_TS.key(), new Date());
        }};

        final Document infoLogDocument = new Document(infoLog);
        final Document errorLogDocument = new Document(errorLog);

        final Map<String, Object> jobLogs = new HashMap<String, Object>() {{
            put(MESSAGES.key(), asList(infoLogDocument, errorLogDocument));
            put(JOB_TYPE.key(), "SomeType");
            put(ID.key(), "/SomeType/ID");
            put(STATUS.key(), ERROR.toString());
        }};

        //when
        final JobInfo jobInfo = repo.decode(new Document(jobLogs));

        //then
        assertThat(jobInfo.getMessages().size(), is(2));
    }

    @Test
    public void shouldFindStatusOfAJob() throws Exception {
        //Given
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO");
        repo.createOrUpdate(foo);

        //When
        final JobStatus status = repo.findStatus("http://localhost/foo");

        //Then
        assertThat(status, is(OK));
    }

    @Test
    public void shouldAppendMessageToJob() throws Exception {
        // given
        final String jobId = "http://localhost/baZ";
        final JobInfo jobInfo = jobInfo(jobId, "T_FOO");
        repo.createOrUpdate(jobInfo);

        // when
        final JobMessage jobMessage = jobMessage(Level.INFO, "Schön ist es auf der Welt zu sein, sagt der Igel zu dem Stachelschwein", now());
        repo.appendMessage(jobId, jobMessage);

        // then
        final JobInfo jobInfoFromDB = repo.findOne(jobId).orElse(null);
        assertThat(jobInfoFromDB.getMessages(), hasSize(3));
        assertThat(jobInfoFromDB.getMessages().get(2), is(jobMessage));
        assertThat(jobInfoFromDB.getStatus(), is(OK));

    }

    @Test
    public void shouldDeleteJobInfos() throws Exception {
        // given
        repo.createOrUpdate(someJobInfo("http://localhost/foo"));

        // when
        repo.deleteAll();

        // then
        assertThat(repo.findAll(), is(Lists.emptyList()));
    }

    @Test
    public void shouldTruncateTooBigJobMessagesArray() throws Exception {
        // given
        final String jobId = "idOfTooBigJob";
        final JobInfo jobInfo = jobInfo(jobId, "BIG_JOB");
        repo.createOrUpdate(jobInfo);

        ch.qos.logback.classic.Level oldLevel = ((Logger) (LoggerFactory.getLogger("org.mongodb.driver"))).getLevel();
        ((Logger)(LoggerFactory.getLogger("org.mongodb.driver"))).setLevel(ch.qos.logback.classic.Level.ERROR);
        // when
        char[] chars = new char[1024 * 1024 * 15 / 1500];
        Arrays.fill(chars, 't');
        for (int i = 0; i < 1500; i++) {
            final JobMessage jobMessage = jobMessage(Level.INFO, new String(chars), now());
            repo.appendMessage(jobId, jobMessage);
        }
        ((Logger)(LoggerFactory.getLogger("org.mongodb.driver"))).setLevel(oldLevel);

        // when
        repo.keepJobMessagesWithinMaximumSize(jobId);

        // then
        final JobInfo jobInfoFromDB = repo.findOne(jobId).orElse(null);
        assertThat(jobInfoFromDB.getMessages(), hasSize(1000));
        assertThat(jobInfoFromDB.getMessages().get(999).getMessage(), containsString("The messages array for this job is growing towards MongoDBs limit for single documents, so I'll drop all messages but the last 1000."));
        assertThat(jobInfoFromDB.getStatus(), is(OK));
    }

    private JobInfo someJobInfo(final String jobId) {
        return JobInfo.newJobInfo(
                jobId,
                "SOME_JOB",
                now(), now(), Optional.of(now()), OK,
                asList(
                        jobMessage(Level.INFO, "foo", now()),
                        jobMessage(Level.WARNING, "bar", now())),
                systemDefaultZone(),
                "localhost"
        );
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
