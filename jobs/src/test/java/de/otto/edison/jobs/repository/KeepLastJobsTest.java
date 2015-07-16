package de.otto.edison.jobs.repository;


import de.otto.edison.jobs.monitor.JobMonitor;
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
import static org.mockito.Mockito.mock;

public class KeepLastJobsTest {

    private final Clock now = fixed(Instant.now(), systemDefault());
    private final Clock earlier = fixed(Instant.now().minusSeconds(1), systemDefault());
    private final Clock muchEarlier = fixed(Instant.now().minusSeconds(10), systemDefault());

    @Test
    public void shouldRemoveJobsWithMatchingJobType() {
        // given
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo("TYPE1", create("foo"), mock(JobMonitor.class), now).stop());
            createOrUpdate(newJobInfo("TYPE2", create("foobar"), mock(JobMonitor.class), now).stop());
            createOrUpdate(newJobInfo("TYPE2", create("bar"), mock(JobMonitor.class), now).stop());
        }};
        KeepLastJobs strategy = new KeepLastJobs(1, Optional.of("TYPE2"));
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(2));
        assertThat(repository.findByType("TYPE2"), hasSize(1));
    }

    @Test
    public void shouldRemoveOldestJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo("TYPE", create("foo"), mock(JobMonitor.class), now).stop());
            createOrUpdate(newJobInfo("TYPE", create("foobar"), mock(JobMonitor.class), earlier).stop());
            createOrUpdate(newJobInfo("TYPE", create("bar"), mock(JobMonitor.class), muchEarlier).stop());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(2));
        assertThat(repository.findBy(create("bar")), isAbsent());
    }

    @Test
    public void shouldOnlyRemoveStoppedJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(1, Optional.of("TYPE"));
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo("TYPE", create("foo"), mock(JobMonitor.class), now).stop());
            createOrUpdate(newJobInfo("TYPE", create("foobar"), mock(JobMonitor.class), earlier));
            createOrUpdate(newJobInfo("TYPE", create("bar"), mock(JobMonitor.class), muchEarlier).stop());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.findBy(create("foo")), isPresent());
        assertThat(repository.findBy(create("foobar")), isPresent());
        assertThat(repository.findBy(create("bar")), isAbsent());

        assertThat(repository.size(), is(2));
    }

    @Test
    public void shouldKeepAtLeastOneSuccessfulJob() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo("TYPE", create("foo"), mock(JobMonitor.class), now).error("bumm").stop());
            createOrUpdate(newJobInfo("TYPE", create("foobar"), mock(JobMonitor.class), muchEarlier).stop());
            createOrUpdate(newJobInfo("TYPE", create("bar"), mock(JobMonitor.class), earlier).error("bumm").stop());
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
        KeepLastJobs strategy = new KeepLastJobs(1, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo("TYPE1", create("foo"), mock(JobMonitor.class), now).stop());
            createOrUpdate(newJobInfo("TYPE2", create("foobar"), mock(JobMonitor.class), muchEarlier));
            createOrUpdate(newJobInfo("TYPE2", create("bar"), mock(JobMonitor.class), earlier));
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(3));
        assertThat(repository.findByType("TYPE2"), hasSize(2));
    }

    @Test
    public void shouldBeOkToKeepAllJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2, Optional.empty());
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo("TYPE", create("foo"), mock(JobMonitor.class), now).stop());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(1));
    }
}