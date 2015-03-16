package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Test
public class StopDeadJobsTest {

    JobType type = () -> "TYPE2";

    @Test
    public void shouldOnlyMarkOldJobAsStopped() throws Exception {
        //given
        JobInfo runningJobToBeStopped = jobInfoBuilder(type, URI.create("runningJobToBeStopped")).withStarted(now().minusSeconds(60)).withLastUpdated(now().minusSeconds(25)).build();
        JobInfo runningJob = jobInfoBuilder(type, URI.create("runningJob")).withStarted(now().minusSeconds(60)).withLastUpdated(now()).build();
        JobInfo stoppedJob = jobInfoBuilder(type, URI.create("stoppedJob")).withStarted(now().minusSeconds(60)).withStopped(now().minusSeconds(30)).build();

        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(runningJobToBeStopped);
            createOrUpdate(runningJob);
            createOrUpdate(stoppedJob);
        }};

        StopDeadJobs strategy = new StopDeadJobs(21);

        //when
        strategy.doCleanUp(repository);

        //then
        JobInfo toBeStopped = repository.findBy(URI.create("runningJobToBeStopped")).get();
        JobInfo running = repository.findBy(URI.create("runningJob")).get();
        JobInfo stopped = repository.findBy(URI.create("stoppedJob")).get();

        assertThat(toBeStopped.getStopped().get(), is(runningJobToBeStopped.getLastUpdated()));
        assertThat(running, is(runningJob));
        assertThat(stopped, is(stoppedJob));
    }
}