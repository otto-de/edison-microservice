package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import org.hamcrest.collection.IsCollectionWithSize;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.net.URI.create;
import static java.time.Clock.fixed;
import static java.time.Clock.systemDefaultZone;
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
        final URI testUri = create("test");
        repository.createOrUpdate(newJobInfo(testUri, "FOO", systemDefaultZone(), "localhost"));
        // when
        repository.removeIfStopped(testUri);
        // then
        assertThat(repository.size(), is(1L));
    }

    @Test
    public void shouldNotFailToRemoveMissingJob() {
        // when
        repository.removeIfStopped(create("foo"));
        // then
        // no Exception is thrown...
    }

    @Test
    public void shouldRemoveJob() throws Exception {
        final URI testUri = create("test");
        final JobInfo jobInfo = newJobInfo(testUri, "FOO", systemDefaultZone(), "localhost").stop();
        repository.createOrUpdate(jobInfo);

        repository.removeIfStopped(jobInfo.getJobUri());

        assertThat(repository.size(), is(0L));
    }

    @Test
    public void shouldFindAll() {
        // given
        repository.createOrUpdate(newJobInfo(create("oldest"), "FOO", fixed(Instant.now().minusSeconds(1), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo(create("youngest"), "FOO", fixed(Instant.now(), systemDefault()), "localhost"));
        // when
        final List<JobInfo> jobInfos = repository.findAll();
        // then
        assertThat(jobInfos.size(), is(2));
        assertThat(jobInfos.get(0).getJobUri(), is(create("youngest")));
        assertThat(jobInfos.get(1).getJobUri(), is(create("oldest")));
    }

    @Test
    public void shouldFindRunningJobsWithoutUpdatedSinceSpecificDate() throws Exception {
        // given
        repository.createOrUpdate(newJobInfo(create("deadJob"), "FOO", fixed(Instant.now().minusSeconds(10), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo(create("running"), "FOO", fixed(Instant.now(), systemDefault()), "localhost"));

        // when
        final List<JobInfo> jobInfos = repository.findRunningWithoutUpdateSince(OffsetDateTime.now().minus(5, ChronoUnit.SECONDS));

        // then
        assertThat(jobInfos, IsCollectionWithSize.hasSize(1));
        assertThat(jobInfos.get(0).getJobUri(), is(create("deadJob")));
    }

    @Test
    public void shouldFindLatestByType() {
        // given
        final String type = "TEST";
        final String otherType = "OTHERTEST";


        repository.createOrUpdate(newJobInfo(create("oldest"), type, fixed(Instant.now().minusSeconds(10), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo(create("other"), otherType, fixed(Instant.now().minusSeconds(5), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo(create("youngest"), type, fixed(Instant.now(), systemDefault()), "localhost"));

        // when
        final List<JobInfo> jobInfos = repository.findLatestBy(type, 2);

        // then
        assertThat(jobInfos.get(0).getJobUri(), is(create("youngest")));
        assertThat(jobInfos.get(1).getJobUri(), is(create("oldest")));
        assertThat(jobInfos, hasSize(2));
    }

    @Test
    public void shouldFindLatest() {
        // given
        final String type = "TEST";
        final String otherType = "OTHERTEST";
        repository.createOrUpdate(newJobInfo(create("oldest"), type, fixed(Instant.now().minusSeconds(10), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo(create("other"), otherType, fixed(Instant.now().minusSeconds(5), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo(create("youngest"), type, fixed(Instant.now(), systemDefault()), "localhost"));

        // when
        final List<JobInfo> jobInfos = repository.findLatest(2);

        // then
        assertThat(jobInfos.get(0).getJobUri(), is(create("youngest")));
        assertThat(jobInfos.get(1).getJobUri(), is(create("other")));
        assertThat(jobInfos, hasSize(2));
    }

    @Test
    public void shouldFindARunningJobGivenAType() throws Exception {
        // given
        final String type = "TEST";
        final String otherType = "OTHERTEST";
        repository.createOrUpdate(newJobInfo(create("some/job/stopped"), type, fixed(Instant.now().minusSeconds(10), systemDefault()), "localhost").stop());
        repository.createOrUpdate(newJobInfo(create("some/job/other"), otherType, fixed(Instant.now().minusSeconds(5), systemDefault()), "localhost"));
        repository.createOrUpdate(newJobInfo(create("some/job/running"), type, fixed(Instant.now(), systemDefault()), "localhost"));

        // when
        final Optional<JobInfo> runningJob = repository.findRunningJobByType(type);

        // then
        assertThat(runningJob.get().getJobUri(), is(URI.create("some/job/running")));
    }

    @Test
    public void shouldReturnNullIfNoRunningJobOfTypePresent() throws Exception {
        // given

        // when
        final Optional<JobInfo> runningJob = repository.findRunningJobByType("someType");

        // then
        assertThat(runningJob.isPresent(), is(false));
    }

    @Test
    public void shouldFindAllJobsOfSpecificType() throws Exception {
        // Given
        final String type = "TEST";
        final String otherType = "OTHERTEST";
        repository.createOrUpdate(newJobInfo(create("1"), type, systemDefaultZone(), "localhost").stop());
        repository.createOrUpdate(newJobInfo(create("2"), otherType, systemDefaultZone(), "localhost"));
        repository.createOrUpdate(newJobInfo(create("3"), type, systemDefaultZone(), "localhost"));

        // When
        final List<JobInfo> jobsType1 = repository.findByType(type);
        final List<JobInfo> jobsType2 = repository.findByType(otherType);

        // Then
        assertThat(jobsType1.size(), is(2));
        assertTrue(jobsType1.stream().anyMatch(job -> job.getJobUri().equals(create("1"))));
        assertTrue(jobsType1.stream().anyMatch(job -> job.getJobUri().equals(create("3"))));
        assertThat(jobsType2.size(), is(1));
        assertTrue(jobsType2.stream().anyMatch(job -> job.getJobUri().equals(create("2"))));
    }

    @Test
    public void shouldFindStatusOfJob() throws Exception {
        //Given
        final String type = "TEST";
        JobInfo jobInfo = newJobInfo(create("1"), type, systemDefaultZone(), "localhost");
        repository.createOrUpdate(jobInfo);

        //When
        JobInfo.JobStatus status = repository.findStatus(create("1"));

        //Then
        assertThat(status, is(JobInfo.JobStatus.OK));
    }
}
