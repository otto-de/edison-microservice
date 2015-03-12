package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.List;

import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static java.net.URI.create;
import static java.time.OffsetDateTime.now;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class InMemJobRepositoryTest {

    @Test
    public void shouldNotRemoveRunningJobs() {
        // given
        final InMemJobRepository repository = new InMemJobRepository();
        final URI testUri = create("test");
        repository.createOrUpdate(
                jobInfoBuilder(() -> "FOO", testUri).build()
        );
        // when
        repository.removeIfStopped(testUri);
        // then
        assertThat(repository.size(), is(1));
    }

    @Test
    public void shouldNotFailToRemoveMissingJob() {
        // given
        InMemJobRepository repository = new InMemJobRepository();
        // when
        repository.removeIfStopped(create("foo"));
        // then
        // no Exception is thrown...
    }

    @Test
    public void shouldOrderYoungestFirst() {
        // given
        final JobType type = () -> "TEST";
        final InMemJobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(type, create("oldest")).withStarted(now().minusSeconds(1)).build());
            createOrUpdate(jobInfoBuilder(type, create("youngest")).build());
        }};
        // when
        final List<JobInfo> jobInfos = repository.findAll();
        // then
        assertThat(jobInfos.get(0).getJobUri(), is(create("youngest")));
        assertThat(jobInfos.get(1).getJobUri(), is(create("oldest")));
    }

    @Test
    public void shouldOrderOldestFirst() {
        // given
        final JobType type = () -> "TEST";
        final InMemJobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(type, create("oldest")).withStarted(now().minusSeconds(1)).build());
            createOrUpdate(jobInfoBuilder(type, create("youngest")).build());
        }};
        // when
        final List<JobInfo> jobInfos = repository.findAll(comparing(JobInfo::getStarted, naturalOrder()));
        // then
        assertThat(jobInfos.get(0).getJobUri(), is(create("oldest")));
        assertThat(jobInfos.get(1).getJobUri(), is(create("youngest")));
    }

    @Test
    public void shouldOrderYoungestOfTypeFirst() {
        // given
        final JobType type = () -> "TEST";
        final JobType otherType = () -> "OTHERTEST";
        final InMemJobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(type, create("oldest")).withStarted(now().minusSeconds(2)).build());
            createOrUpdate(jobInfoBuilder(otherType, create("other")).withStarted(now().minusSeconds(1)).build());
            createOrUpdate(jobInfoBuilder(type, create("youngest")).build());
        }};
        // when
        final List<JobInfo> jobInfos = repository.findBy(type);
        // then
        assertThat(jobInfos.get(0).getJobUri(), is(create("youngest")));
        assertThat(jobInfos.get(1).getJobUri(), is(create("oldest")));
        assertThat(jobInfos, hasSize(2));
    }

    @Test
    public void shouldOrderOldestOfTypeFirst() {
        // given
        final JobType type = () -> "TEST";
        final JobType otherType = () -> "OTHERTEST";
        final InMemJobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(type, create("oldest")).withStarted(now().minusSeconds(2)).build());
            createOrUpdate(jobInfoBuilder(otherType, create("other")).withStarted(now().minusSeconds(1)).build());
            createOrUpdate(jobInfoBuilder(type, create("youngest")).build());
        }};
        // when
        final List<JobInfo> jobInfos = repository.findBy(type, comparing(JobInfo::getStarted, naturalOrder()));
        // then
        assertThat(jobInfos.get(0).getJobUri(), is(create("oldest")));
        assertThat(jobInfos.get(1).getJobUri(), is(create("youngest")));
        assertThat(jobInfos, hasSize(2));
    }

}