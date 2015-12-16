package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.cleanup.StopDeadJobs;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.DEAD;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.net.URI.create;
import static java.time.Clock.fixed;
import static java.time.ZoneId.systemDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@Test
public class StopDeadJobsTest {


    @Test
    public void shouldOnlyMarkOldJobAsStopped() throws Exception {
        //given
        final Clock clock = fixed(Instant.now(), systemDefault());
        final Clock earlierClock = fixed(Instant.now().minusSeconds(25), systemDefault());

        JobInfo runningJobToBeStopped = newJobInfo(create("runningJobToBeStopped"), "TYPE", earlierClock);
        JobInfo runningJob = newJobInfo(create("runningJob"), "TYPE", clock);
        JobInfo stoppedJob = newJobInfo(create("stoppedJob"), "TYPE", earlierClock).stop();

        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(runningJobToBeStopped);
            createOrUpdate(runningJob);
            createOrUpdate(stoppedJob);
        }};

        StopDeadJobs strategy = new StopDeadJobs(21, clock);
        strategy.setJobRepository(repository);

        //when
        strategy.doCleanUp();

        //then
        JobInfo toBeStopped = repository.findOne(URI.create("runningJobToBeStopped")).get();
        JobInfo running = repository.findOne(URI.create("runningJob")).get();
        JobInfo stopped = repository.findOne(URI.create("stoppedJob")).get();

        assertThat(toBeStopped.getStopped().get(), is(OffsetDateTime.now(earlierClock)));
        assertThat(toBeStopped.getLastUpdated(), is(OffsetDateTime.now(earlierClock)));
        assertThat(toBeStopped.getStatus(), is(DEAD));
        assertThat(toBeStopped.getMessages().get(0).getMessage(), is(notNullValue()));
        assertThat(running, is(runningJob));
        assertThat(stopped, is(stoppedJob));

    }
}