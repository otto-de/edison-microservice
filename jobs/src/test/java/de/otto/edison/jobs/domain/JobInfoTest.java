package de.otto.edison.jobs.domain;

import org.testng.annotations.Test;

import java.net.URI;

import static de.otto.edison.testsupport.matcher.OptionalMatchers.isAbsent;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;

public class JobInfoTest {

    @Test
    public void shouldInitializeCorrectly() {
        JobInfo job = new JobInfo(() -> "TEST", URI.create("foo"));
        assertThat(job.getState(), is(JobInfo.ExecutionState.RUNNING));
        assertThat(job.getStatus(), is(JobInfo.JobStatus.OK));
        assertThat(job.getJobUri(), is(URI.create("foo")));
        assertThat(job.getJobType().name(), is("TEST"));
        assertThat(job.getStarted().getHour(), is(now().getHour()));
        assertThat(job.getStopped(), isAbsent());
        assertThat(job.getMessages(), is(emptyIterable()));
    }

}