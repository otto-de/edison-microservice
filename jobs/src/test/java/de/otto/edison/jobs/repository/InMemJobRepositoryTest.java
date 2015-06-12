package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import org.hamcrest.collection.IsCollectionWithSize;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static java.net.URI.create;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
                jobInfoBuilder("FOO", testUri).build()
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
        JobInfo jobInfo = jobInfoBuilder("FULL_IMPORT", create("oldest")).withStopped(OffsetDateTime.now()).build();
        repository.createOrUpdate(jobInfo);

        repository.removeIfStopped(jobInfo.getJobUri());

        assertThat(repository.size(), is(0));
    }

    @Test
    public void shouldFindAll() {
        // given
        final String type = "TEST";
        repository.createOrUpdate(jobInfoBuilder(type, create("oldest")).withStarted(now().minusSeconds(1)).build());
        repository.createOrUpdate(jobInfoBuilder(type, create("youngest")).build());
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
        final String type = "TEST";
        final OffsetDateTime someTime = now();

        repository.createOrUpdate(jobInfoBuilder(type, create("deadJob")).withLastUpdated(someTime.minusHours(2)).build());
        repository.createOrUpdate(jobInfoBuilder(type, create("running")).withLastUpdated(someTime).build());
        repository.createOrUpdate(jobInfoBuilder(type, create("stopped")).withStopped(someTime.minusMinutes(10)).withLastUpdated(someTime.minusHours(1)).build());

        // when
        final List<JobInfo> jobInfos = repository.findRunningWithoutUpdateSince(OffsetDateTime.now().minus(1, ChronoUnit.HOURS));

        // then
        assertThat(jobInfos, IsCollectionWithSize.hasSize(1));
        assertThat(jobInfos.get(0).getJobUri(), is(create("deadJob")));
    }

    @Test
    public void shouldFindLatestByType() {
        // given
        final String type = "TEST";
        final String otherType = "OTHERTEST";


        repository.createOrUpdate(jobInfoBuilder(type, create("oldest")).withStarted(now().minusSeconds(2)).build());
        repository.createOrUpdate(jobInfoBuilder(otherType, create("other")).withStarted(now().minusSeconds(1)).build());
        repository.createOrUpdate(jobInfoBuilder(type, create("youngest")).build());

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
        repository.createOrUpdate(jobInfoBuilder(type, create("oldest")).withStarted(now().minusSeconds(2)).build());
        repository.createOrUpdate(jobInfoBuilder(otherType, create("other")).withStarted(now().minusSeconds(1)).build());
        repository.createOrUpdate(jobInfoBuilder(type, create("youngest")).build());

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
        final OffsetDateTime someTime = now();
        final String someType = "someType";

        repository.createOrUpdate(jobInfoBuilder(someType, create("some/job/oldest")).withStopped(someTime.minusSeconds(2)).build());
        repository.createOrUpdate(jobInfoBuilder(someType, create("some/job/running")).build());
        repository.createOrUpdate(jobInfoBuilder("someOtherType", create("some/other/job")).build());

        // when
        final JobInfo runningJob = repository.findRunningJobByType(someType);

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
        final String type1 = "TYPE1";
        final String type2 = "TYPE2";

        repository.createOrUpdate(jobInfoBuilder(type1, create("1")).build());
        repository.createOrUpdate(jobInfoBuilder(type2, create("2")).build());
        repository.createOrUpdate(jobInfoBuilder(type1, create("3")).build());

        // When
        final List<JobInfo> jobsType1 = repository.findByType(type1);
        final List<JobInfo> jobsType2 = repository.findByType(type2);

        // Then
        assertThat(jobsType1.size(), is(2));
        assertTrue(jobsType1.stream().anyMatch(job -> job.getJobUri().equals(create("1"))));
        assertTrue(jobsType1.stream().anyMatch(job -> job.getJobUri().equals(create("3"))));
        assertThat(jobsType2.size(), is(1));
        assertTrue(jobsType2.stream().anyMatch(job -> job.getJobUri().equals(create("2"))));
    }
}
