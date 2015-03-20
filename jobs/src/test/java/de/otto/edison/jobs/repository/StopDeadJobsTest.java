package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;
import de.otto.edison.jobs.service.Clock;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.OffsetDateTime;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.DEAD;
import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static de.otto.edison.jobs.repository.StopDeadJobs.*;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test
public class StopDeadJobsTest {

    private static final JobType type = () -> "TYPE2";

    @Test
    public void shouldOnlyMarkOldJobAsStopped() throws Exception {
        //given
        OffsetDateTime now = now();
        JobInfo runningJobToBeStopped = jobInfoBuilder(type, URI.create("runningJobToBeStopped")).withStarted(now.minusSeconds(60)).withLastUpdated(now.minusSeconds(25)).build();
        JobInfo runningJob = jobInfoBuilder(type, URI.create("runningJob")).withStarted(now.minusSeconds(60)).withLastUpdated(now).build();
        JobInfo stoppedJob = jobInfoBuilder(type, URI.create("stoppedJob")).withStarted(now.minusSeconds(60)).withStopped(now.minusSeconds(30)).build();

        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(runningJobToBeStopped);
            createOrUpdate(runningJob);
            createOrUpdate(stoppedJob);
        }};

        Clock clock = mock(Clock.class);
        when(clock.now()).thenReturn(now);

        StopDeadJobs strategy = new StopDeadJobs(21, clock);

        //when
        strategy.doCleanUp(repository);

        //then
        JobInfo toBeStopped = repository.findBy(URI.create("runningJobToBeStopped")).get();
        JobInfo running = repository.findBy(URI.create("runningJob")).get();
        JobInfo stopped = repository.findBy(URI.create("stoppedJob")).get();

        assertThat(toBeStopped.getStopped().get(), is(now));
        assertThat(toBeStopped.getLastUpdated(), is(now));
        assertThat(toBeStopped.getStatus(), is(DEAD));
        assertThat(toBeStopped.getMessages().get(0).getMessage(),is(JOB_DEAD_MESSAGE));
        assertThat(running, is(runningJob));
        assertThat(stopped, is(stoppedJob));

    }
}