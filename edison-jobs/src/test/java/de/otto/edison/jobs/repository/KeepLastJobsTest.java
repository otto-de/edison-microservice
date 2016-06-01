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
    private final Clock evenEarlier = fixed(Instant.now().minusSeconds(20), systemDefault());

    @Test
    public void shouldRemoveOldestJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2);
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo("foo", "TYPE", now, "localhost").stop());
            createOrUpdate(newJobInfo("foobar", "TYPE", earlier, "localhost").stop());
            createOrUpdate(newJobInfo("bar", "TYPE", muchEarlier, "localhost").stop());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(2L));
        assertThat(repository.findOne("bar"), isAbsent());
    }

    @Test
    public void shouldOnlyRemoveStoppedJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(1);
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo("foo", "TYPE", now, "localhost").stop());
            createOrUpdate(newJobInfo("foobar", "TYPE", earlier, "localhost"));
            createOrUpdate(newJobInfo("bar", "TYPE", muchEarlier, "localhost").stop());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.findOne("foo"), isPresent());
        assertThat(repository.findOne("foobar"), isPresent());
        assertThat(repository.findOne("bar"), isAbsent());

        assertThat(repository.size(), is(2L));
    }

    @Test
    public void shouldKeepAtLeastOneSuccessfulJob() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2);
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo("foo", "TYPE", now, "localhost").error("bumm").stop());
            createOrUpdate(newJobInfo("bar", "TYPE", earlier, "localhost").error("bumm").stop());
            createOrUpdate(newJobInfo("barzig", "TYPE", evenEarlier, "localhost").error("b00m!!1shakalaka").stop());
            createOrUpdate(newJobInfo("foobar", "TYPE", muchEarlier, "localhost").stop());
            createOrUpdate(newJobInfo("foozification", "TYPE", evenEarlier, "localhost").error("b00m!!1").stop());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(3L));
        assertThat(repository.findOne("foo"), isPresent());
        assertThat(repository.findOne("bar"), isPresent());
        assertThat(repository.findOne("barzig"), isAbsent());
        assertThat(repository.findOne("foobar"), isPresent());
        assertThat(repository.findOne("foozification"), isAbsent());
    }

    @Test
    public void shouldKeepNJobsOfEachTypePresentAndNotRemoveRunningJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2);
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo("foo1", "TYPE1", now, "localhost").stop());
            createOrUpdate(newJobInfo("foo2", "TYPE1", muchEarlier, "localhost").stop());
            createOrUpdate(newJobInfo("foo3", "TYPE1", evenEarlier, "localhost").stop());
            createOrUpdate(newJobInfo("bar1", "TYPE2", earlier, "localhost")).stop();
            createOrUpdate(newJobInfo("bar2", "TYPE2", muchEarlier, "localhost")).stop();
            createOrUpdate(newJobInfo("bar3", "TYPE2", evenEarlier, "localhost")).stop();
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(4L));
        assertThat(repository.findByType("TYPE1"), hasSize(2));
        assertThat(repository.findByType("TYPE2"), hasSize(2));
    }

    @Test
    public void shouldBeOkToKeepAllJobs() {
        // given
        KeepLastJobs strategy = new KeepLastJobs(2);
        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(newJobInfo("foo", "TYPE", now, "localhost").stop());
        }};
        strategy.setJobRepository(repository);
        // when
        strategy.doCleanUp();
        // then
        assertThat(repository.size(), is(1L));
    }
}