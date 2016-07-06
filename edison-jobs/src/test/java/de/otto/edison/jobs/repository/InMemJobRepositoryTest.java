package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import org.hamcrest.collection.IsCollectionWithSize;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.builder;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.time.Clock.fixed;
import static java.time.Clock.systemDefaultZone;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertTrue;

public class InMemJobRepositoryTest {

    InMemJobRepository repository;

    @BeforeMethod
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
    public void shouldFindLatestAndStatus() {
        // given
        final String type = "TEST";
        final String otherType = "OTHERTEST";
        repository.createOrUpdate(newJobInfo("oldest", type, fixed(Instant.now().minusSeconds(10), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo("other", otherType, fixed(Instant.now().minusSeconds(5), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo("youngest", type, fixed(Instant.now(), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo("finished", type, now(), now(), Optional.of(now()),
                JobStatus.OK, Collections.emptyList(), systemDefaultZone(), "localhost"));

        // when
        final List<JobInfo> jobInfos = repository.findLatestFinishedBy(type, JobStatus.OK, 2);

        // then
        assertThat(jobInfos.size(), is(1));
        assertThat(jobInfos.get(0).getJobId(), is("finished"));
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
        assertTrue(jobsType1.stream().anyMatch(job -> job.getJobId().equals("1")));
        assertTrue(jobsType1.stream().anyMatch(job -> job.getJobId().equals("3")));
        assertThat(jobsType2.size(), is(1));
        assertTrue(jobsType2.stream().anyMatch(job -> job.getJobId().equals("2")));
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
}
