package de.otto.edison.jobs.repository.dynamo;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.testsupport.mongo.EmbeddedMongoHelper;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.ERROR;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobInfo.builder;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isAbsent;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isPresent;
import static java.time.Clock.fixed;
import static java.time.Clock.systemDefaultZone;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.util.Lists.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
public class DynamoJobRepositoryTest {

    private static DynamoJobRepository testee;
    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    public static void initDbs() throws IOException {
        testee = new DynamoJobRepository(getDynamoDbClient(), objectMapper);
    }

    @Container
    static GenericContainer dynamodb = new GenericContainer("amazon/dynamodb-local:latest")
            .withExposedPorts(8000);


    @BeforeEach
    public void setUpDynamo() {
        testee.setupSchema();
    }

    @AfterEach
    public void tearDown() {
        testee.deleteTable();
    }

    @BeforeEach
    public void setUp() throws Exception {
        testee = new DynamoJobRepository(getDynamoDbClient(), objectMapper);
    }

    private static DynamoDbClient getDynamoDbClient() {
        String endpointUri = "http://" + dynamodb.getContainerIpAddress() + ":" +
                dynamodb.getMappedPort(8000);

        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpointUri))
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials.create("acc", "sec"))).build();
    }


    private Clock clock = systemDefaultZone();

    @Test
    public void shouldCreateOrUpdateJob() {
        //Given
        final JobInfo savedjobInfo = jobInfo("http://localhost/foo", "T_FOO");
        testee.createOrUpdate(savedjobInfo);

        //When
        testee.createOrUpdate(savedjobInfo);

        final Optional<JobInfo> readJobInfo = testee.findOne(savedjobInfo.getJobId());

        //Then
        assertThat(readJobInfo.get(), equalTo(savedjobInfo));
    }

    @Test
    public void shouldFindJobInfoByUri() {
        // given
        DynamoJobRepository repository = new DynamoJobRepository(getDynamoDbClient(), objectMapper);

        // when
        JobInfo job = newJobInfo(randomUUID().toString(), "MYJOB", clock, "localhost");
        repository.createOrUpdate(job);

        // then
        assertThat(repository.findOne(job.getJobId()), isPresent());
    }

    @Test
    public void shouldReturnAbsentStatus() {
        DynamoJobRepository repository = new DynamoJobRepository(getDynamoDbClient(), objectMapper);
        assertThat(repository.findOne("some-nonexisting-job-id"), isAbsent());
    }

    @Test
    public void shouldNotFailToRemoveMissingJob() {
        // when
        testee.removeIfStopped("foo");
        // then
        // no Exception is thrown...
    }

//    @Test
//    public void shouldNotRemoveRunningJobs() {
//        // given
//        final String testUri = "test";
//        testee.createOrUpdate(newJobInfo(testUri, "FOO", systemDefaultZone(), "localhost"));
//        // when
//        testee.removeIfStopped(testUri);
//        // then
//        assertThat(testee.size(), is(1L));

//    }

    @Test
    public void shouldRemoveJob() throws Exception {
        JobInfo stoppedJob = builder()
                .setJobId("some/job/stopped")
                .setJobType("test")
                .setStarted(now(fixed(Instant.now().minusSeconds(10), systemDefault())))
                .setStopped(now(fixed(Instant.now().minusSeconds(7), systemDefault())))
                .setHostname("localhost")
                .setStatus(JobStatus.OK)
                .build();
        testee.createOrUpdate(stoppedJob);
        testee.createOrUpdate(stoppedJob);

        testee.removeIfStopped(stoppedJob.getJobId());

        assertThat(testee.size(), is(0L));
    }

    @Test
    public void shouldFindAll() {
        // given
        testee.createOrUpdate(newJobInfo("oldest", "FOO", fixed(Instant.now().minusSeconds(1), systemDefault()), "localhost"));
        testee.createOrUpdate(newJobInfo("youngest", "FOO", fixed(Instant.now(), systemDefault()), "localhost"));
        // when
        final List<JobInfo> jobInfos = testee.findAll();
        // then
        assertThat(jobInfos.size(), is(2));
        assertThat(jobInfos.get(0).getJobId(), is("youngest"));
        assertThat(jobInfos.get(1).getJobId(), is("oldest"));
    }

    @Test
    public void shouldFindAllinSizeOperation() {
        // given
        testee.createOrUpdate(newJobInfo("oldest", "FOO", fixed(Instant.now().minusSeconds(1), systemDefault()), "localhost"));
        testee.createOrUpdate(newJobInfo("youngest", "FOO", fixed(Instant.now(), systemDefault()), "localhost"));
        // when
        final long count = testee.size();
        // then
        assertThat(count, is(2L));
    }

    @Test
    public void shouldFindAllinSizeOperationWithPageing() {
        // given
        testee.createOrUpdate(newJobInfo("oldest", "FOO", fixed(Instant.now().minusSeconds(1), systemDefault()), "localhost"));
        testee.createOrUpdate(newJobInfo("youngest", "FOO", fixed(Instant.now(), systemDefault()), "localhost"));
        testee.createOrUpdate(newJobInfo("youn44444556gest", "FOO", fixed(Instant.now(), systemDefault()), "localhost"));
        testee.createOrUpdate(newJobInfo("yo121ungest", "FOO", fixed(Instant.now(), systemDefault()), "localhost"));
        testee.createOrUpdate(newJobInfo("youn333333gest", "FOO", fixed(Instant.now(), systemDefault()), "localhost"));
        testee.createOrUpdate(newJobInfo("youn1212gest", "FOO", fixed(Instant.now(), systemDefault()), "localhost"));
        // when
        final long count = testee.size();
        // then
        //TODO write better test with paging enabled in scan
        assertThat(count, is(7L));
    }

//
//    @Test
//    public void shouldFindLatestDistinct() throws Exception {
//        // Given
//        Instant now = Instant.now();
//        final JobInfo eins = newJobInfo("eins", "eins", fixed(now.plusSeconds(10), systemDefault()), "localhost");
//        final JobInfo zwei = newJobInfo("zwei", "eins", fixed(now.plusSeconds(20), systemDefault()), "localhost");
//        final JobInfo drei = newJobInfo("drei", "zwei", fixed(now.plusSeconds(30), systemDefault()), "localhost");
//        final JobInfo vier = newJobInfo("vier", "drei", fixed(now.plusSeconds(40), systemDefault()), "localhost");
//        final JobInfo fuenf = newJobInfo("fuenf", "drei", fixed(now.plusSeconds(50), systemDefault()), "localhost");
//
//        testee.createOrUpdate(eins);
//        testee.createOrUpdate(zwei);
//        testee.createOrUpdate(drei);
//        testee.createOrUpdate(vier);
//        testee.createOrUpdate(fuenf);
//
//        // When
//        List<JobInfo> latestDistinct = testee.findLatestJobsDistinct();
//
//        // Then
//        assertThat(latestDistinct, hasSize(3));
//        assertThat(latestDistinct, Matchers.containsInAnyOrder(fuenf, zwei, drei));
//    }
//
//
//
//    @Test
//    public void shouldFindRunningJobsWithoutUpdatedSinceSpecificDate() throws Exception {
//        // given
//        testee.createOrUpdate(newJobInfo("deadJob", "FOO", fixed(Instant.now().minusSeconds(10), systemDefault()), "localhost"));
//        testee.createOrUpdate(newJobInfo("running", "FOO", fixed(Instant.now(), systemDefault()), "localhost"));
//
//        // when
//        final List<JobInfo> jobInfos = testee.findRunningWithoutUpdateSince(now().minus(5, ChronoUnit.SECONDS));
//
//        // then
//        assertThat(jobInfos, IsCollectionWithSize.hasSize(1));
//        assertThat(jobInfos.get(0).getJobId(), is("deadJob"));
//    }
//
//    @Test
//    public void shouldFindLatestByType() {
//        // given
//        final String type = "TEST";
//        final String otherType = "OTHERTEST";
//
//
//        testee.createOrUpdate(newJobInfo("oldest", type, fixed(Instant.now().minusSeconds(10), systemDefault()), "localhost"));
//        testee.createOrUpdate(newJobInfo("other", otherType, fixed(Instant.now().minusSeconds(5), systemDefault()), "localhost"));
//        testee.createOrUpdate(newJobInfo("youngest", type, fixed(Instant.now(), systemDefault()), "localhost"));
//
//        // when
//        final List<JobInfo> jobInfos = testee.findLatestBy(type, 2);
//
//        // then
//        assertThat(jobInfos.get(0).getJobId(), is("youngest"));
//        assertThat(jobInfos.get(1).getJobId(), is("oldest"));
//        assertThat(jobInfos, hasSize(2));
//    }
//
//    @Test
//    public void shouldFindLatest() {
//        // given
//        final String type = "TEST";
//        final String otherType = "OTHERTEST";
//        testee.createOrUpdate(newJobInfo("oldest", type, fixed(Instant.now().minusSeconds(10), systemDefault()), "localhost"));
//        testee.createOrUpdate(newJobInfo("other", otherType, fixed(Instant.now().minusSeconds(5), systemDefault()), "localhost"));
//        testee.createOrUpdate(newJobInfo("youngest", type, fixed(Instant.now(), systemDefault()), "localhost"));
//
//        // when
//        final List<JobInfo> jobInfos = testee.findLatest(2);
//
//        // then
//        assertThat(jobInfos.get(0).getJobId(), is("youngest"));
//        assertThat(jobInfos.get(1).getJobId(), is("other"));
//        assertThat(jobInfos, hasSize(2));
//    }
//
//    @Test
//    public void shouldFindAllJobsOfSpecificType() throws Exception {
//        // Given
//        final String type = "TEST";
//        final String otherType = "OTHERTEST";
//        testee.createOrUpdate(builder()
//                .setJobId("1")
//                .setJobType(type)
//                .setStarted(now(fixed(Instant.now().minusSeconds(10), systemDefault())))
//                .setStopped(now(fixed(Instant.now().minusSeconds(7), systemDefault())))
//                .setHostname("localhost")
//                .setStatus(JobStatus.OK)
//                .build());
//        testee.createOrUpdate(newJobInfo("2", otherType, systemDefaultZone(), "localhost"));
//        testee.createOrUpdate(newJobInfo("3", type, systemDefaultZone(), "localhost"));
//
//        // When
//        final List<JobInfo> jobsType1 = testee.findByType(type);
//        final List<JobInfo> jobsType2 = testee.findByType(otherType);
//
//        // Then
//        assertThat(jobsType1.size(), is(2));
//        assertThat(jobsType1.stream().anyMatch(job -> job.getJobId().equals("1")), is(true));
//        assertThat(jobsType1.stream().anyMatch(job -> job.getJobId().equals("3")), is(true));
//        assertThat(jobsType2.size(), is(1));
//        assertThat(jobsType2.stream().anyMatch(job -> job.getJobId().equals("2")), is(true));
//    }
//
//    @Test
//    public void shouldFindStatusOfJob() throws Exception {
//        //Given
//        final String type = "TEST";
//        JobInfo jobInfo = newJobInfo("1", type, systemDefaultZone(), "localhost");
//        testee.createOrUpdate(jobInfo);
//
//        //When
//        JobStatus status = testee.findStatus("1");
//
//        //Then
//        assertThat(status, is(JobStatus.OK));
//    }
//
//    @Test
//    public void shouldAppendMessageToJobInfo() throws Exception {
//
//        String someUri = "someUri";
//        OffsetDateTime now = now();
//
//        //Given
//        JobInfo jobInfo = newJobInfo(someUri, "TEST", systemDefaultZone(), "localhost");
//        testee.createOrUpdate(jobInfo);
//
//        //When
//        JobMessage igelMessage = JobMessage.jobMessage(Level.WARNING, "Der Igel ist froh.", now);
//        testee.appendMessage(someUri, igelMessage);
//
//        //Then
//        JobInfo jobInfoFromRepo = testee.findOne(someUri).get();
//
//        assertThat(jobInfoFromRepo.getMessages().size(), is(1));
//        assertThat(jobInfoFromRepo.getMessages().get(0), is(igelMessage));
//        assertThat(jobInfoFromRepo.getLastUpdated(), is(now.truncatedTo(ChronoUnit.MILLIS)));
//
//    }
//
//    @Test
//    public void shouldUpdateJobStatus() {
//        //Given
//        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO"); //default jobStatus is 'OK'
//        testee.createOrUpdate(foo);
//
//        //When
//        testee.setJobStatus(foo.getJobId(), ERROR);
//        JobStatus status = testee.findStatus("http://localhost/foo");
//
//        //Then
//        assertThat(status, is(ERROR));
//    }
//
//
//
//    @Test
//    public void shouldUpdateJobLastUpdateTime() {
//        //Given
//        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO");
//        testee.createOrUpdate(foo);
//
//        OffsetDateTime myTestTime = OffsetDateTime.of(1979, 2, 5, 1, 2, 3, 1_000_000, ZoneOffset.UTC);
//
//        //When
//        testee.setLastUpdate(foo.getJobId(), myTestTime);
//
//        final Optional<JobInfo> jobInfo = testee.findOne(foo.getJobId());
//
//        //Then
//        assertThat(jobInfo.get().getLastUpdated(), is(myTestTime));
//    }
//
//    @Test
//    public void shouldClearJobInfos() throws Exception {
//        //Given
//        JobInfo stoppedJob = builder()
//                .setJobId("some/job/stopped")
//                .setJobType("test")
//                .setStarted(now(fixed(Instant.now().minusSeconds(10), systemDefault())))
//                .setStopped(now(fixed(Instant.now().minusSeconds(7), systemDefault())))
//                .setHostname("localhost")
//                .setStatus(JobStatus.OK)
//                .build();
//        testee.createOrUpdate(stoppedJob);
//
//        //When
//        testee.deleteAll();
//
//        //Then
//        assertThat(testee.findAll(), is(emptyList()));
//    }

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
}
