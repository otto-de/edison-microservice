package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.cleanup.StopDeadJobs;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import de.otto.edison.jobs.service.JobMutexHandler;
import org.testng.annotations.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.DEAD;
import static de.otto.edison.jobs.domain.JobInfo.builder;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.time.Clock.fixed;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Test
public class StopDeadJobsTest {


    @Test
    public void shouldOnlyMarkOldJobAsStopped() throws Exception {
        //given
        final Clock clock = fixed(Instant.now(), systemDefault());
        final Clock earlierClock = fixed(Instant.now().minusSeconds(25), systemDefault());

        JobInfo runningJobToBeStopped = newJobInfo("runningJobToBeStopped", "runningJobToBeStoppedTYPE", earlierClock, "localhost");
        JobInfo runningJob = newJobInfo("runningJob", "runningJobTYPE", clock, "localhost");

        JobInfo stoppedJob = builder()
                .setJobId("stoppedJob")
                .setJobType("stoppedJobTYPE")
                .setStarted(now(earlierClock))
                .setStopped(now(earlierClock))
                .setHostname("localhost")
                .setStatus(JobInfo.JobStatus.OK)
                .build();

        JobRepository repository = new InMemJobRepository() {{
            createOrUpdate(runningJobToBeStopped);
            createOrUpdate(runningJob);
            createOrUpdate(stoppedJob);
        }};
        JobMutexHandler jobMutexHandler = mock(JobMutexHandler.class);

        StopDeadJobs strategy = new StopDeadJobs(21, clock);
        strategy.setJobRepository(repository);
        strategy.setJobMutexHandler(jobMutexHandler);

        //when
        strategy.doCleanUp();

        //then
        JobInfo toBeStopped = repository.findOne("runningJobToBeStopped").get();
        JobInfo running = repository.findOne("runningJob").get();
        JobInfo stopped = repository.findOne("stoppedJob").get();
        assertThat(toBeStopped.getStopped().get(), is(OffsetDateTime.now(earlierClock)));
        assertThat(toBeStopped.getLastUpdated(), is(OffsetDateTime.now(earlierClock)));
        assertThat(toBeStopped.getStatus(), is(DEAD));
        assertThat(toBeStopped.getMessages().get(0).getMessage(), is(notNullValue()));
        assertThat(running, is(runningJob));
        assertThat(stopped, is(stoppedJob));
        verify(jobMutexHandler).jobHasStopped("runningJobToBeStoppedTYPE");
    }
}