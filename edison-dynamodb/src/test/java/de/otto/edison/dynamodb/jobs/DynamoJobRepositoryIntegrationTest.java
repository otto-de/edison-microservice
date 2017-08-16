package de.otto.edison.dynamodb.jobs;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.ERROR;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static java.time.Clock.systemDefaultZone;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ComponentScan(basePackages = {"de.otto.edison.dynamodb"})
@EnableAutoConfiguration
@ActiveProfiles("test")
public class DynamoJobRepositoryIntegrationTest {

    @Autowired
    private DynamoJobRepository dynamoJobRepository;

    @Before
    public void setUp() {
        dynamoJobRepository.createTable();
        dynamoJobRepository.deleteAll();
    }

    @Test
    public void shouldStoreAndRetrieveJobInfo() {
        // given
        final JobInfo foo = someJobInfo("http://localhost/foo/A");
        final JobInfo writtenFoo = dynamoJobRepository.create(foo);
        // when
        final Optional<JobInfo> jobInfo = dynamoJobRepository.findOne("http://localhost/foo/A");
        // then
        assertThat(jobInfo.isPresent(), is(true));
        assertThat(jobInfo.orElse(null), is(writtenFoo));
    }

    @Test
    public void shouldUpdateJobInfo() {
        // given
        final JobInfo foo = someJobInfo("http://localhost/foo/B");
        dynamoJobRepository.createOrUpdate(foo);
        final JobInfo writtenFoo = dynamoJobRepository.createOrUpdate(
                foo.copy().addMessage(jobMessage(Level.INFO, "some message", now(foo.getClock()))).build());
        // when
        final Optional<JobInfo> jobInfo = dynamoJobRepository.findOne("http://localhost/foo/B");
        // then
        assertThat(jobInfo.orElse(null), is(writtenFoo));
    }

    @Test
    public void shouldUpdateJobStatus() {
        //Given
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO"); //default jobStatus is 'OK'
        dynamoJobRepository.createOrUpdate(foo);

        //When
        dynamoJobRepository.setJobStatus(foo.getJobId(), ERROR);
        final JobStatus status = dynamoJobRepository.findStatus("http://localhost/foo");

        //Then
        assertThat(status, is(ERROR));
    }

    @Test
    public void shouldUpdateJobLastUpdateTime() {
        //Given
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO");
        dynamoJobRepository.createOrUpdate(foo);

        final OffsetDateTime myTestTime = OffsetDateTime.of(1979, 2, 5, 1, 2, 3, 0, ZoneOffset.UTC);

        //When
        dynamoJobRepository.setLastUpdate(foo.getJobId(), myTestTime);

        final Optional<JobInfo> jobInfo = dynamoJobRepository.findOne(foo.getJobId());

        //Then
        assertThat(OffsetDateTime.ofInstant(Instant.ofEpochMilli(jobInfo.get().getLastUpdated().toInstant().toEpochMilli()),
                ZoneId.ofOffset("UTC", ZoneOffset.UTC)), is(myTestTime));
    }

    @Test
    public void shouldStoreAndRetrieveRunningJobInfo() {
        // given
        final JobInfo foo = someRunningJobInfo("http://localhost/foo", "SOME_JOB", now());
        dynamoJobRepository.createOrUpdate(foo);
        // when
        final Optional<JobInfo> jobInfo = dynamoJobRepository.findOne("http://localhost/foo");
        // then
        assertThat(jobInfo.orElse(null), is(foo));
    }

    @Test
    public void shouldStoreAndRetrieveAllJobInfo() {
        // given
        dynamoJobRepository.createOrUpdate(someJobInfo("http://localhost/foo"));
        dynamoJobRepository.createOrUpdate(someJobInfo("http://localhost/bar"));
        // when
        final List<JobInfo> jobInfos = dynamoJobRepository.findAll();
        // then
        assertThat(jobInfos, hasSize(2));
    }

    @Test
    public void shouldStoreAndRetrieveAllJobInfoWithoutMessages() {
        // given
        final JobInfo job1 = someJobInfo("http://localhost/foo");
        final JobInfo job2 = someJobInfo("http://localhost/bar");

        dynamoJobRepository.createOrUpdate(job1);
        dynamoJobRepository.createOrUpdate(job2);
        // when
        final List<JobInfo> jobInfos = dynamoJobRepository.findAllJobInfoWithoutMessages();
        // then
        assertThat(jobInfos, hasSize(2));
        assertThat(jobInfos.get(0), is(job1.copy().setMessages(emptyList()).build()));
        assertThat(jobInfos.get(1), is(job2.copy().setMessages(emptyList()).build()));
    }

    @Test
    public void shouldFindLatest() {
        // given
        dynamoJobRepository.createOrUpdate(someRunningJobInfo("http://localhost/foo", "SOME_JOB", now()));
        final JobInfo later = someRunningJobInfo("http://localhost/bar", "SOME_JOB", now().plus(1, SECONDS));
        dynamoJobRepository.createOrUpdate(later);
        final JobInfo evenLater = someRunningJobInfo("http://localhost/foobar", "SOME_JOB", now().plus(2, SECONDS));
        dynamoJobRepository.createOrUpdate(evenLater);
        // when
        final List<JobInfo> jobInfos = dynamoJobRepository.findLatest(2);
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
        dynamoJobRepository.createOrUpdate(oldest);
        dynamoJobRepository.createOrUpdate(later);
        dynamoJobRepository.createOrUpdate(evenLater);
        // when
        final List<JobInfo> jobInfos = dynamoJobRepository.findLatestBy("SOME_JOB", 2);
        // then
        assertThat(jobInfos, containsInAnyOrder(oldest, evenLater));
    }

    @Test
    public void shouldFindByType() {
        // given
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO");
        dynamoJobRepository.createOrUpdate(foo);
        dynamoJobRepository.createOrUpdate(jobInfo("http://localhost/bar", "T_BAR"));
        // when
        final List<JobInfo> jobInfos = dynamoJobRepository.findByType("T_FOO");
        // then
        assertThat(jobInfos, contains(foo));
    }

    @Test
    public void shouldFindRunningWithoutUpdateSince() {
        // given
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO");
        final JobInfo bar = someRunningJobInfo("http://localhost/bar", "T_BAR", now());
        final JobInfo foobar = someRunningJobInfo("http://localhost/foobar", "T_BAR", now().plusSeconds(3));
        dynamoJobRepository.createOrUpdate(foo);
        dynamoJobRepository.createOrUpdate(bar);
        dynamoJobRepository.createOrUpdate(foobar);
        // when
        final List<JobInfo> infos = dynamoJobRepository.findRunningWithoutUpdateSince(now().plusSeconds(2));
        // then
        assertThat(infos, hasSize(1));
        assertThat(infos.get(0), is(bar));
    }

    @Test
    public void shouldRemoveIfStopped() {
        // given
        final JobInfo foo = jobInfo("foo", "T_FOO");
        final JobInfo bar = someRunningJobInfo("bar", "T_BAR", now());
        dynamoJobRepository.createOrUpdate(foo);
        dynamoJobRepository.createOrUpdate(bar);
        // when
        dynamoJobRepository.removeIfStopped("foo");
        dynamoJobRepository.removeIfStopped("bar");
        // then
        assertThat(dynamoJobRepository.findAll(), hasSize(1));
        assertThat(dynamoJobRepository.findAll(), contains(bar));
    }

    @Test
    public void shouldFindLatestDistinct() throws Exception {
        // Given
        final OffsetDateTime now = now();
        final JobInfo eins = someRunningJobInfo("http://localhost/eins", "someJobType", now);
        final JobInfo zwei = someRunningJobInfo("http://localhost/zwei", "someOtherJobType", now.plusSeconds(1));
        final JobInfo drei = someRunningJobInfo("http://localhost/drei", "nextJobType", now.plusSeconds(2));
        final JobInfo vier = someRunningJobInfo("http://localhost/vier", "someJobType", now.plusSeconds(3));
        final JobInfo fuenf = someRunningJobInfo("http://localhost/fuenf", "someJobType", now.plusSeconds(4));

        dynamoJobRepository.createOrUpdate(eins);
        dynamoJobRepository.createOrUpdate(zwei);
        dynamoJobRepository.createOrUpdate(drei);
        dynamoJobRepository.createOrUpdate(vier);
        dynamoJobRepository.createOrUpdate(fuenf);

        // When
        final List<JobInfo> latestDistinct = dynamoJobRepository.findLatestJobsDistinct();

        // Then
        assertThat(latestDistinct, hasSize(3));
        assertThat(latestDistinct, containsInAnyOrder(fuenf, zwei, drei));
    }

    @Test
    public void shouldFindStatusOfAJob() throws Exception {
        //Given
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO");
        dynamoJobRepository.createOrUpdate(foo);

        //When
        final JobStatus status = dynamoJobRepository.findStatus("http://localhost/foo");

        //Then
        assertThat(status, is(OK));
    }

    @Test
    public void shouldAppendMessageToJob() throws Exception {
        // given
        final String jobId = "http://localhost/baZ";
        final JobInfo jobInfo = jobInfo(jobId, "T_FOO");
        dynamoJobRepository.createOrUpdate(jobInfo);

        // when
        final JobMessage jobMessage = jobMessage(Level.INFO, "Schön ist es auf der Welt zu sein, sagt der Igel zu dem Stachelschwein",
                now());
        dynamoJobRepository.appendMessage(jobId, jobMessage);

        // then
        final JobInfo jobInfoFromDB = dynamoJobRepository.findOne(jobId).orElse(null);
        assertThat(jobInfoFromDB.getMessages(), hasSize(3));
        assertThat(jobInfoFromDB.getMessages().get(2), is(jobMessage));
        assertThat(jobInfoFromDB.getStatus(), is(OK));
    }

    @Test
    public void shouldDeleteJobInfos() throws Exception {
        // given
        dynamoJobRepository.createOrUpdate(someJobInfo("http://localhost/foo"));

        // when
        dynamoJobRepository.deleteAll();

        // then
        assertThat(dynamoJobRepository.findAll(), is(Lists.emptyList()));
    }

    private JobInfo someJobInfo(final String jobId) {
        return JobInfo.newJobInfo(jobId, "SOME_JOB", now(), now(), Optional.of(now()), OK,
                asList(jobMessage(Level.INFO, "foo", now()), jobMessage(Level.WARNING, "bar", now())), systemDefaultZone(), "localhost");
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
                emptyList(),
                systemDefaultZone(),
                "localhost"
        );
    }
}
