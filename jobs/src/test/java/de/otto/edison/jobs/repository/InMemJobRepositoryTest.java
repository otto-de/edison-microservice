package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.monitor.JobMonitor;
import org.hamcrest.collection.IsCollectionWithSize;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.net.URI.create;
import static java.time.Clock.fixed;
import static java.time.Clock.systemDefaultZone;
import static java.time.ZoneId.systemDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
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
        repository.createOrUpdate(
                newJobInfo("FOO", testUri, mock(JobMonitor.class), systemDefaultZone())
        );
        // when
        repository.removeIfStopped(testUri);
        // then
        assertThat(repository.size(), is(1));
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
        final JobInfo jobInfo = newJobInfo("FOO", testUri, mock(JobMonitor.class), systemDefaultZone()).stop();
        repository.createOrUpdate(jobInfo);

        repository.removeIfStopped(jobInfo.getJobUri());

        assertThat(repository.size(), is(0));
    }

    @Test
    public void shouldFindAll() {
        // given
        repository.createOrUpdate(newJobInfo("FOO", create("oldest"), mock(JobMonitor.class), fixed(Instant.now().minusSeconds(1), systemDefault())));
        repository.createOrUpdate(newJobInfo("FOO", create("youngest"), mock(JobMonitor.class), fixed(Instant.now(), systemDefault())));
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
        repository.createOrUpdate(newJobInfo("FOO", create("deadJob"), mock(JobMonitor.class), fixed(Instant.now().minusSeconds(10), systemDefault())));
        repository.createOrUpdate(newJobInfo("FOO", create("running"), mock(JobMonitor.class), fixed(Instant.now(), systemDefault())));

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


        repository.createOrUpdate(newJobInfo(type, create("oldest"), mock(JobMonitor.class), fixed(Instant.now().minusSeconds(10), systemDefault())));
        repository.createOrUpdate(newJobInfo(otherType, create("other"), mock(JobMonitor.class), fixed(Instant.now().minusSeconds(5), systemDefault())));
        repository.createOrUpdate(newJobInfo(type, create("youngest"), mock(JobMonitor.class), fixed(Instant.now(), systemDefault())));

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
        repository.createOrUpdate(newJobInfo(type, create("oldest"), mock(JobMonitor.class), fixed(Instant.now().minusSeconds(10), systemDefault())));
        repository.createOrUpdate(newJobInfo(otherType, create("other"), mock(JobMonitor.class), fixed(Instant.now().minusSeconds(5), systemDefault())));
        repository.createOrUpdate(newJobInfo(type, create("youngest"), mock(JobMonitor.class), fixed(Instant.now(), systemDefault())));

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
        repository.createOrUpdate(newJobInfo(type, create("some/job/stopped"), mock(JobMonitor.class), fixed(Instant.now().minusSeconds(10), systemDefault())).stop());
        repository.createOrUpdate(newJobInfo(otherType, create("some/job/other"), mock(JobMonitor.class), fixed(Instant.now().minusSeconds(5), systemDefault())));
        repository.createOrUpdate(newJobInfo(type, create("some/job/running"), mock(JobMonitor.class), fixed(Instant.now(), systemDefault())));

        // when
        final JobInfo runningJob = repository.findRunningJobByType(type);

        // then
        assertThat(runningJob.getJobUri(), is(URI.create("some/job/running")));
    }

    @Test
    public void shouldReturnNullIfNoRunningJobOfTypePresent() throws Exception {
        // given
        
        // when
        final JobInfo runningJob = repository.findRunningJobByType("someType");

        // then
        assertThat(runningJob, is(nullValue()));
    }

    @Test
    public void shouldFindAllJobsOfSpecificType() throws Exception {
        // Given
        final String type = "TEST";
        final String otherType = "OTHERTEST";
        repository.createOrUpdate(newJobInfo(type, create("1"), mock(JobMonitor.class), systemDefaultZone()).stop());
        repository.createOrUpdate(newJobInfo(otherType, create("2"), mock(JobMonitor.class), systemDefaultZone()));
        repository.createOrUpdate(newJobInfo(type, create("3"), mock(JobMonitor.class), systemDefaultZone()));

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
}
