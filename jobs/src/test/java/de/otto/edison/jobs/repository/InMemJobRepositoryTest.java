package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isPresent;
import static java.time.LocalDateTime.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class InMemJobRepositoryTest {

    @Test
    public void shouldNotDeleteRunningJobs() {
        // given
        InMemJobRepository repository = new InMemJobRepository();
        JobType jobType = () -> "FOO";
        repository.createOrUpdate(
                jobInfoBuilder(jobType, URI.create("test")).build()
        );
        // when
        repository.deleteOldest(of(jobType));
        // then
        assertThat(repository.size(), is(1));
    }

    @Test
    public void shouldNotFailToDeleteMissingJob() {
        // given
        InMemJobRepository repository = new InMemJobRepository();
        // when
        repository.deleteOldest(of((JobType) () -> "FOO"));
        repository.deleteOldest(empty());
        // then
        // no Exception is thrown...
    }

    @Test
    public void shouldDeleteStoppedJob() {
        // given
        InMemJobRepository repository = new InMemJobRepository();
        JobType jobType = () -> "FOO";
        repository.createOrUpdate(
                jobInfoBuilder(jobType, URI.create("test")).withStopped(now()).build()
        );
        // when
        repository.deleteOldest(of(jobType));
        // then
        assertThat(repository.size(), is(0));
    }

    @Test
    public void shouldDeleteOldestStoppedJobOfType() {
        // given
        InMemJobRepository repository = new InMemJobRepository();
        JobType jobType = () -> "FOO";
        repository.createOrUpdate(
                jobInfoBuilder(jobType, URI.create("42")).withStarted(now().minusSeconds(1)).withStopped(now()).build()
        );
        JobInfo expectedSurvivor = jobInfoBuilder(jobType, URI.create("0815")).withStopped(now()).build();
        repository.createOrUpdate(
                expectedSurvivor
        );
        // when
        repository.deleteOldest(of(jobType));
        // then
        assertThat(repository.size(), is(1));
        assertThat(repository.findBy(expectedSurvivor.getJobUri()), isPresent());
    }

    @Test
    public void shouldDeleteOldestStoppedJob() {
        // given
        InMemJobRepository repository = new InMemJobRepository();
        repository.createOrUpdate(
                jobInfoBuilder(() -> "FOO", URI.create("42")).withStarted(now().minusSeconds(1)).withStopped(now()).build()
        );
        JobInfo expectedSurvivor = jobInfoBuilder(() -> "BAR", URI.create("0815")).withStopped(now()).build();
        repository.createOrUpdate(
                expectedSurvivor
        );
        // when
        Optional<JobInfo> deleted = repository.deleteOldest(empty());
        // then
        assertThat(repository.size(), is(1));
        assertThat(deleted, isPresent());
        assertThat(repository.findBy(expectedSurvivor.getJobUri()), isPresent());
    }

}