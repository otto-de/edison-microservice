package de.otto.edison.jobs.repository;


import de.otto.edison.jobs.repository.cleanup.KeepLastJobs;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import org.testng.annotations.Test;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isAbsent;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isPresent;
import static java.net.URI.create;
import static java.time.Clock.fixed;
import static java.time.ZoneId.systemDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class KeepLastJobsTest {

    private final Clock now = fixed(Instant.now(), systemDefault());
    private final Clock earlier = fixed(Instant.now().minusSeconds(1), systemDefault());
    private final Clock muchEarlier = fixed(Instant.now().minusSeconds(10), systemDefault());

    @Test
    public void shouldRemoveJobsWithMatchingJobType() {
        // given
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo(create("foo"), "TYPE1", now).stop());
            createOrUpdate(newJobInfo(create("foobar"), "TYPE2", now).stop());
            createOrUpdate(newJobInfo(create("bar"), "TYPE2", now).stop());
        }};
        KeepLastJobs strategy = new KeepLastJobs(1, Optional.of("TYPE2"));
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(2L));
        assertThat(repository.findByType("TYPE2"), hasSize(1));
    }

    @Test
    public void shouldRemoveOldestJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo(create("foo"), "TYPE", now).stop());
            createOrUpdate(newJobInfo(create("foobar"), "TYPE", earlier).stop());
            createOrUpdate(newJobInfo(create("bar"), "TYPE", muchEarlier).stop());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(2L));
        assertThat(repository.findOne(create("bar")), isAbsent());
    }

    @Test
    public void shouldOnlyRemoveStoppedJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(1, Optional.of("TYPE"));
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo(create("foo"), "TYPE", now).stop());
            createOrUpdate(newJobInfo(create("foobar"), "TYPE", earlier));
            createOrUpdate(newJobInfo(create("bar"), "TYPE", muchEarlier).stop());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.findOne(create("foo")), isPresent());
        assertThat(repository.findOne(create("foobar")), isPresent());
        assertThat(repository.findOne(create("bar")), isAbsent());

        assertThat(repository.size(), is(2L));
    }

    @Test
    public void shouldKeepAtLeastOneSuccessfulJob() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo(create("foo"), "TYPE", now).error("bumm").stop());
            createOrUpdate(newJobInfo(create("foobar"), "TYPE", muchEarlier).stop());
            createOrUpdate(newJobInfo(create("bar"), "TYPE", earlier).error("bumm").stop());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(2L));
        assertThat(repository.findOne(create("foobar")), isPresent());
        assertThat(repository.findOne(create("foo")), isPresent());
        assertThat(repository.findOne(create("bar")), isAbsent());
    }

    @Test
    public void shouldKeep1JobOfEachTypePresentAndNotRemoveRunningJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(1, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo(create("foo"), "TYPE1", now).stop());
            createOrUpdate(newJobInfo(create("foobar"), "TYPE2", muchEarlier));
            createOrUpdate(newJobInfo(create("bar"), "TYPE2", earlier));
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(3L));
        assertThat(repository.findByType("TYPE2"), hasSize(2));
    }

    @Test
    public void shouldBeOkToKeepAllJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo(create("foo"), "TYPE", now).stop());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(1L));
    }
}