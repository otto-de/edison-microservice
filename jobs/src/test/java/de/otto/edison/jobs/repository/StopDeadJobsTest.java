package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.monitor.JobMonitor;
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
import static org.mockito.Mockito.mock;

@Test
public class StopDeadJobsTest {


    @Test
    public void shouldOnlyMarkOldJobAsStopped() throws Exception {
        //given
        final Clock clock = fixed(Instant.now(), systemDefault());
        final Clock earlierClock = fixed(Instant.now().minusSeconds(25), systemDefault());

        JobInfo runningJobToBeStopped = newJobInfo("TYPE", create("runningJobToBeStopped"), mock(JobMonitor.class), earlierClock);
        JobInfo runningJob = newJobInfo("TYPE", create("runningJob"), mock(JobMonitor.class), clock);
        JobInfo stoppedJob = newJobInfo("TYPE", create("stoppedJob"), mock(JobMonitor.class), earlierClock).stop();

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
        JobInfo toBeStopped = repository.findBy(URI.create("runningJobToBeStopped")).get();
        JobInfo running = repository.findBy(URI.create("runningJob")).get();
        JobInfo stopped = repository.findBy(URI.create("stoppedJob")).get();

        assertThat(toBeStopped.getStopped().get(), is(OffsetDateTime.now(earlierClock)));
        assertThat(toBeStopped.getLastUpdated(), is(OffsetDateTime.now(earlierClock)));
        assertThat(toBeStopped.getStatus(), is(DEAD));
        assertThat(toBeStopped.getMessages().get(0).getMessage(),is(notNullValue()));
        assertThat(running, is(runningJob));
        assertThat(stopped, is(stoppedJob));

    }
}