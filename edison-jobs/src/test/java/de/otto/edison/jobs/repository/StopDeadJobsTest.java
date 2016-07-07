package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.cleanup.StopDeadJobs;
import de.otto.edison.jobs.service.JobService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.DEAD;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.time.Clock.fixed;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@Test
public class StopDeadJobsTest {
    static final String JOB_ID = "runningJobToBeStopped";

    StopDeadJobs subject;

    @Mock
    JobService jobServiceMock;

    @Mock
    JobRepository jobRepository;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        subject = new StopDeadJobs(jobServiceMock, jobRepository, 21, Clock.systemDefaultZone());

    }

    @Test
    public void shouldMarkOldJobAsDeadAndStopped() throws Exception {
        //given
        final Clock earlierClock = fixed(Instant.now().minusSeconds(25), systemDefault());

        JobInfo runningJobToBeStopped = newJobInfo(JOB_ID, "runningJobToBeStoppedTYPE", earlierClock, "localhost");

        when(jobRepository.findRunningWithoutUpdateSince(any())).thenReturn(asList(runningJobToBeStopped));

        //when
        subject.doCleanUp();

        //then
        verify(jobServiceMock).killJob(JOB_ID);
    }
}