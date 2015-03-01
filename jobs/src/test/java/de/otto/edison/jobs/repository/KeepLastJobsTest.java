package de.otto.edison.jobs.repository;


import de.otto.edison.jobs.domain.JobType;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static java.time.LocalDateTime.now;
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