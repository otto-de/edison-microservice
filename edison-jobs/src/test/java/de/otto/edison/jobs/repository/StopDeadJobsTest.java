package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.cleanup.StopDeadJobs;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.DEAD;
import static de.otto.edison.jobs.domain.JobInfo.builder;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.time.Clock.fixed;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

@Test
public class StopDeadJobsTest {


    @Test
    public void shouldMarkOldJobAsDeadAndStopped() throws Exception {
        //given
        final Clock clock = fixed(Instant.now(), systemDefault());
        final Clock earlierClock = fixed(Instant.now().minusSeconds(25), systemDefault());

        JobInfo runningJobToBeStopped = newJobInfo("runningJobToBeStopped", "runningJobToBeStoppedTYPE", earlierClock, "localhost");

        JobRepository repository = Mockito.mock(JobRepository.class);
        when(repository.findRunningWithoutUpdateSince(any())).thenReturn(asList(runningJobToBeStopped));

        StopDeadJobs strategy = new StopDeadJobs(21, clock);
        strategy.setJobRepository(repository);

        //when
        strategy.doCleanUp();

        //then
        verify(repository).createOrUpdate(runningJobToBeStopped.copy().setStatus(DEAD).setStopped(OffsetDateTime.now(earlierClock)).setLastUpdated(OffsetDateTime.now(earlierClock)).build());
        verify(repository).stopJob(runningJobToBeStopped);
    }
}