package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Before;
import org.junit.Test;

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
import static java.time.Clock.fixed;
import static java.time.Clock.systemDefaultZone;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Arrays.asList;
import static org.assertj.core.util.Lists.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class InMemJobRepositoryTest {

    InMemJobRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = new InMemJobRepository();
    }


    @Test
    public void shouldNotRemoveRunningJobs() {
        // given
        final String testUri = "test";
        repository.createOrUpdate(newJobInfo(testUri, "FOO", systemDefaultZone(), "localhost"));
        // when
        repository.removeIfStopped(testUri);
        // then
        assertThat(repository.size(), is(1L));
    }

    @Test
    public void shouldNotFailToRemoveMissingJob() {
        // when
        repository.removeIfStopped("foo");
        // then
        // no Exception is thrown...
    }

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
        repository.createOrUpdate(stoppedJob);
        repository.createOrUpdate(stoppedJob);

        repository.removeIfStopped(stoppedJob.getJobId());

        assertThat(repository.size(), is(0L));
    }

    @Test
    public void shouldFindAll() {
        // given
        repository.createOrUpdate(newJobInfo("oldest", "FOO", fixed(Instant.now().minusSeconds(1), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo("youngest", "FOO", fixed(Instant.now(), systemDefault()), "localhost"));
        // when
        final List<JobInfo> jobInfos = repository.findAll();
        // then
        assertThat(jobInfos.size(), is(2));
        assertThat(jobInfos.get(0).getJobId(), is("youngest"));
        assertThat(jobInfos.get(1).getJobId(), is("oldest"));
    }

    @Test
    public void shouldFindLatestDistinct() throws Exception {
        // Given
        Instant now = Instant.now();
        final JobInfo eins = newJobInfo("eins", "eins", fixed(now.plusSeconds(10), systemDefault()), "localhost");
        final JobInfo zwei = newJobInfo("zwei", "eins", fixed(now.plusSeconds(20), systemDefault()), "localhost");
        final JobInfo drei = newJobInfo("drei", "zwei", fixed(now.plusSeconds(30), systemDefault()), "localhost");
        final JobInfo vier = newJobInfo("vier", "drei", fixed(now.plusSeconds(40), systemDefault()), "localhost");
        final JobInfo fuenf = newJobInfo("fuenf", "drei", fixed(now.plusSeconds(50), systemDefault()), "localhost");

        repository.createOrUpdate(eins);
        repository.createOrUpdate(zwei);
        repository.createOrUpdate(drei);
        repository.createOrUpdate(vier);
        repository.createOrUpdate(fuenf);

        // When
        List<JobInfo> latestDistinct = repository.findLatestJobsDistinct();

        // Then
        assertThat(latestDistinct, hasSize(3));
        assertThat(latestDistinct, Matchers.containsInAnyOrder(fuenf, zwei, drei));
    }



    @Test
    public void shouldFindRunningJobsWithoutUpdatedSinceSpecificDate() throws Exception {
        // given
        repository.createOrUpdate(newJobInfo("deadJob", "FOO", fixed(Instant.now().minusSeconds(10), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo("running", "FOO", fixed(Instant.now(), systemDefault()), "localhost"));

        // when
        final List<JobInfo> jobInfos = repository.findRunningWithoutUpdateSince(now().minus(5, ChronoUnit.SECONDS));

        // then
        assertThat(jobInfos, IsCollectionWithSize.hasSize(1));
        assertThat(jobInfos.get(0).getJobId(), is("deadJob"));
    }

    @Test
    public void shouldFindLatestByType() {
        // given
        final String type = "TEST";
        final String otherType = "OTHERTEST";


        repository.createOrUpdate(newJobInfo("oldest", type, fixed(Instant.now().minusSeconds(10), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo("other", otherType, fixed(Instant.now().minusSeconds(5), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo("youngest", type, fixed(Instant.now(), systemDefault()), "localhost"));

        // when
        final List<JobInfo> jobInfos = repository.findLatestBy(type, 2);

        // then
        assertThat(jobInfos.get(0).getJobId(), is("youngest"));
        assertThat(jobInfos.get(1).getJobId(), is("oldest"));
        assertThat(jobInfos, hasSize(2));
    }

    @Test
    public void shouldFindLatest() {
        // given
        final String type = "TEST";
        final String otherType = "OTHERTEST";
        repository.createOrUpdate(newJobInfo("oldest", type, fixed(Instant.now().minusSeconds(10), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo("other", otherType, fixed(Instant.now().minusSeconds(5), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo("youngest", type, fixed(Instant.now(), systemDefault()), "localhost"));

        // when
        final List<JobInfo> jobInfos = repository.findLatest(2);

        // then
        assertThat(jobInfos.get(0).getJobId(), is("youngest"));
        assertThat(jobInfos.get(1).getJobId(), is("other"));
        assertThat(jobInfos, hasSize(2));
    }

    @Test
    public void shouldFindAllJobsOfSpecificType() throws Exception {
        // Given
        final String type = "TEST";
        final String otherType = "OTHERTEST";
        repository.createOrUpdate(builder()
                .setJobId("1")
                .setJobType(type)
                .setStarted(now(fixed(Instant.now().minusSeconds(10), systemDefault())))
                .setStopped(now(fixed(Instant.now().minusSeconds(7), systemDefault())))
                .setHostname("localhost")
                .setStatus(JobStatus.OK)
                .build());
        repository.createOrUpdate(newJobInfo("2", otherType, systemDefaultZone(), "localhost"));
        repository.createOrUpdate(newJobInfo("3", type, systemDefaultZone(), "localhost"));

        // When
        final List<JobInfo> jobsType1 = repository.findByType(type);
        final List<JobInfo> jobsType2 = repository.findByType(otherType);

        // Then
        assertThat(jobsType1.size(), is(2));
        assertThat(jobsType1.stream().anyMatch(job -> job.getJobId().equals("1")), is(true));
        assertThat(jobsType1.stream().anyMatch(job -> job.getJobId().equals("3")), is(true));
        assertThat(jobsType2.size(), is(1));
        assertThat(jobsType2.stream().anyMatch(job -> job.getJobId().equals("2")), is(true));
    }

    @Test
    public void shouldFindStatusOfJob() throws Exception {
        //Given
        final String type = "TEST";
        JobInfo jobInfo = newJobInfo("1", type, systemDefaultZone(), "localhost");
        repository.createOrUpdate(jobInfo);

        //When
        JobStatus status = repository.findStatus("1");

        //Then
        assertThat(status, is(JobStatus.OK));
    }

    @Test
    public void shouldAppendMessageToJobInfo() throws Exception {

        String someUri = "someUri";

        //Given
        JobInfo jobInfo = newJobInfo(someUri, "TEST", systemDefaultZone(), "localhost");
        repository.createOrUpdate(jobInfo);

        //When
        JobMessage igelMessage = JobMessage.jobMessage(Level.WARNING, "Der Igel ist froh.", now());
        repository.appendMessage(someUri, igelMessage);

        //Then
        JobInfo jobInfoFromRepo = repository.findOne(someUri).get();

        assertThat(jobInfoFromRepo.getMessages().size(), is(1));
        assertThat(jobInfoFromRepo.getMessages().get(0), is(igelMessage));

    }

    @Test
    public void shouldUpdateJobStatus() {
        //Given
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO"); //default jobStatus is 'OK'
        repository.createOrUpdate(foo);

        //When
        repository.setJobStatus(foo.getJobId(), ERROR);
        JobStatus status = repository.findStatus("http://localhost/foo");

        //Then
        assertThat(status, is(ERROR));
    }

    @Test
    public void shouldUpdateJobLastUpdateTime() {
        //Given
        final JobInfo foo = jobInfo("http://localhost/foo", "T_FOO");
        repository.createOrUpdate(foo);

        OffsetDateTime myTestTime = OffsetDateTime.of(1979, 2, 5, 1, 2, 3, 4, ZoneOffset.UTC);

        //When
        repository.setLastUpdate(foo.getJobId(), myTestTime);

        final Optional<JobInfo> jobInfo = repository.findOne(foo.getJobId());

        //Then
        assertThat(jobInfo.get().getLastUpdated(), is(myTestTime));
    }

    @Test
    public void shouldClearJobInfos() throws Exception {
        //Given
        JobInfo stoppedJob = builder()
                .setJobId("some/job/stopped")
                .setJobType("test")
                .setStarted(now(fixed(Instant.now().minusSeconds(10), systemDefault())))
                .setStopped(now(fixed(Instant.now().minusSeconds(7), systemDefault())))
                .setHostname("localhost")
                .setStatus(JobStatus.OK)
                .build();
        repository.createOrUpdate(stoppedJob);

        //When
        repository.deleteAll();

        //Then
        assertThat(repository.findAll(), is(emptyList()));
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
}
