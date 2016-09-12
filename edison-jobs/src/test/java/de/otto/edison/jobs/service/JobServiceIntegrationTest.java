package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.testsupport.TestServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.DEAD;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

@SpringBootTest(classes = TestServer.class)
public class JobServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    JobService jobService;

    @Autowired
    JobRepository jobRepository;

    final Clock clock = Clock.systemDefaultZone();

    @Test
    public void shouldFindJobService() {
        assertThat(jobService, is(notNullValue()));
    }

    @Test
    public void shouldKillJobsWithoutUpdateSince() {
        JobInfo toBeKilled = defaultJobInfo("toBeKilled", 75);
        jobRepository.createOrUpdate(toBeKilled);

        jobService.killJobsDeadSince(60);

        Optional<JobInfo> expectedKilledJob = jobRepository.findOne(toBeKilled.getJobId());
        assertThat(expectedKilledJob.get().isStopped(), is(true));
        assertThat(expectedKilledJob.get().getStatus(), is(DEAD));
    }

    @Test
    public void shouldNotKillJobsThatHaveRecentlyBeenUpdated() {
        JobInfo notToBeKilled = defaultJobInfo("notToBeKilled", 45);
        jobRepository.createOrUpdate(notToBeKilled);

        jobService.killJobsDeadSince(60);

        Optional<JobInfo> expectedRunningJob = jobRepository.findOne(notToBeKilled.getJobId());
        assertThat(expectedRunningJob.get().isStopped(), is(false));
        assertThat(expectedRunningJob.get().getStatus(), is(OK));
    }

    private JobInfo defaultJobInfo(String jobId, int secondsAgo) {
        OffsetDateTime lastUpdated = OffsetDateTime.ofInstant(now(clock).minus(secondsAgo, SECONDS), systemDefault());
        return newJobInfo(jobId, "someJobType", OffsetDateTime.MIN, lastUpdated, Optional.empty(), OK, emptyList(), clock, "someHostname");
    }
}
