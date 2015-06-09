package de.otto.edison.jobs.repository;


import org.testng.annotations.Test;

import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.ERROR;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isAbsent;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isPresent;
import static java.net.URI.create;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class KeepLastJobsTest {




    @Test
    public void shouldRemoveJobsWithMatchingJobType() {
        // given
        String type = "TYPE2";
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder("TYPE1", create("foo")).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, create("foobar")).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, create("bar")).withStopped(now()).build());
        }};
        KeepLastJobs strategy = new KeepLastJobs(1, Optional.of(type));
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(2));
        assertThat(repository.findByType(type), hasSize(1));
    }

    @Test
    public void shouldRemoveOldestJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder("TYPE", create("foo")).withStarted(now()).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder("TYPE", create("foobar")).withStarted(now().minusSeconds(2)).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder("TYPE", create("bar")).withStarted(now().minusSeconds(1)).withStopped(now()).build());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(2));
        assertThat(repository.findBy(create("foobar")), isAbsent());
    }

    @Test
    public void shouldOnlyRemoveStoppedJobs() {
        // given
        String type = "TYPE";
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.of(type));
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(type, create("one")).withStarted(now().minusSeconds(4)).build());
            createOrUpdate(jobInfoBuilder(type, create("two")).withStarted(now().minusSeconds(3)).build());
            createOrUpdate(jobInfoBuilder(type, create("three")).withStarted(now().minusSeconds(2)).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, create("four")).withStarted(now().minusSeconds(1)).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, create("five")).withStarted(now()).withStopped(now()).build());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.findBy(create("one")), isPresent());
        assertThat(repository.findBy(create("two")), isPresent());
        assertThat(repository.findBy(create("three")), isAbsent());
        assertThat(repository.findBy(create("four")), isAbsent());
        assertThat(repository.findBy(create("five")), isPresent());

        assertThat(repository.size(), is(3));
    }

    @Test
    public void shouldKeepAllRunningJobs() {
        // given
        String type = "TYPE";
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(type, create("one")).withStarted(now().minusSeconds(5)).build());
            createOrUpdate(jobInfoBuilder(type, create("two")).withStarted(now().minusSeconds(4)).build());
            createOrUpdate(jobInfoBuilder(type, create("three")).withStarted(now().minusSeconds(3)).build());
            createOrUpdate(jobInfoBuilder(type, create("four")).withStarted(now().minusSeconds(2)).withStopped(now().minusSeconds(1)).build());
            createOrUpdate(jobInfoBuilder(type, create("five")).withStarted(now()).withStopped(now()).build());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.findBy(create("one")), isPresent());
        assertThat(repository.findBy(create("two")), isPresent());
        assertThat(repository.findBy(create("three")), isPresent());

        assertThat(repository.findBy(create("four")), isAbsent());
        assertThat(repository.findBy(create("five")), isPresent());

        assertThat(repository.size(), is(4));
    }

    @Test
    public void shouldKeepAtLeastOneSuccessfulJob() {
        // given
        String type = "TYPE";
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder(type, create("foo")).withStarted(now()).withStatus(ERROR).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, create("foobar")).withStarted(now().minusSeconds(2)).withStatus(OK).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, create("bar")).withStarted(now().minusSeconds(1)).withStatus(ERROR).withStopped(now()).build());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(2));
        assertThat(repository.findBy(create("foobar")), isPresent());
        assertThat(repository.findBy(create("foo")), isPresent());
        assertThat(repository.findBy(create("bar")), isAbsent());
    }

    @Test
    public void shouldKeep1JobOfEachTypePresentAndNotRemoveRunningJobs() {
        // given
        String type = "TYPE2";
        KeepLastJobs strategy = new KeepLastJobs(1, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder("TYPE1", create("foo")).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder(type, create("foobar")).build());
            createOrUpdate(jobInfoBuilder(type, create("bar")).build());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(3));
        assertThat(repository.findByType(type), hasSize(2));
    }

    @Test
    public void shouldKeepTwoJobInfos() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder("TYPE", create("foo")).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder("TYPE", create("foobar")).withStopped(now()).build());
            createOrUpdate(jobInfoBuilder("TYPE", create("bar")).withStopped(now()).build());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(2));
    }

    @Test
    public void shouldBeOkToKeepAllJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(jobInfoBuilder("TYPE", create("bar")).withStopped(now()).build());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(1));
    }
}