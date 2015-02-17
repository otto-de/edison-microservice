package de.otto.µservice.jobs.service;


import de.otto.µservice.jobs.domain.JobInfo;
import org.testng.annotations.Test;

import static de.otto.µservice.jobs.domain.JobInfo.ExecutionState.RUNNING;
import static de.otto.µservice.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.µservice.testsupport.matcher.OptionalMatchers.isAbsent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JobFactoryTest {

    @Test
    public void shouldCreateJob() {
        JobFactory jobFactory = new JobFactory("/foo");
        JobInfo job = jobFactory.createJob(() -> "BAR");

        assertThat(job.getJobUri().toString(), startsWith("/foo/jobs/"));
        assertThat(job.getJobType().name(), is("BAR"));
        assertThat(job.getStarted(), is(notNullValue()));
        assertThat(job.getState(), is(RUNNING));
        assertThat(job.getStatus(), is(OK));
        assertThat(job.getStopped(), isAbsent());
    }

}