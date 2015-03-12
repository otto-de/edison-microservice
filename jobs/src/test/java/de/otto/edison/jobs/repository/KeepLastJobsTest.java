package de.otto.edison.jobs.repository;


import de.otto.edison.jobs.domain.JobType;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isAbsent;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class KeepLastJobsTest {


    @Test
    public void shouldRemoveJobsWithMatchingJobType() {
        // given
        JobType type = () -> "TYPE2";
        KeepLastJobs strategy = new KeepLastJobs(1, Optional.of(type));
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(() -> "TYPE1", URI.create("foo")).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, URI.create("foobar")).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, URI.create("bar")).withStopped(now()).build());
        }};
        // when
        strategy.doCleanUp(repository);
        // then
        assertThat(repository.size(), is(2));
        assertThat(repository.findBy(type), hasSize(1));
    }

    @Test
    public void shouldRemoveOldestJobsWithMatchingJobType() {
        // given
        JobType type = () -> "TYPE";
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.of(type));
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(type, URI.create("foo")).withStarted(now()).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, URI.create("foobar")).withStarted(now().minusSeconds(2)).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, URI.create("bar")).withStarted(now().minusSeconds(1)).withStopped(now()).build());
        }};
        // when
        strategy.doCleanUp(repository);
        // then
        assertThat(repository.size(), is(2));
        assertThat(repository.findBy(URI.create("foobar")), isAbsent());
    }

    @Test
    public void shouldOnlyRemoveStoppedJobsWithMatchingJobType() {
        // given
        JobType type = () -> "TYPE2";
        KeepLastJobs strategy = new KeepLastJobs(1, Optional.of(type));
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(() -> "TYPE1", URI.create("foo")).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, URI.create("foobar")).build());
            createOrUpdate(jobInfoBuilder(type, URI.create("bar")).build());
        }};
        // when
        strategy.doCleanUp(repository);
        // then
        assertThat(repository.size(), is(3));
        assertThat(repository.findBy(type), hasSize(2));
    }

    @Test
    public void shouldKeepTwoJobInfos() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.<JobType>empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(() -> "TYPE", URI.create("foo")).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(() -> "TYPE", URI.create("foobar")).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(() -> "TYPE", URI.create("bar")).withStopped(now()).build());
        }};
        // when
        strategy.doCleanUp(repository);
        // then
        assertThat(repository.size(), is(2));
    }

    @Test
    public void shouldBeOkToKeepAllJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.<JobType>empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(() -> "TYPE", URI.create("bar")).withStopped(now()).build());
        }};
        // when
        strategy.doCleanUp(repository);
        // then
        assertThat(repository.size(), is(1));
    }

}