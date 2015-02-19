package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;
import de.otto.edison.jobs.repository.InMemJobRepository;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.ExecutionState.STOPPED;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isPresent;
import static java.net.URI.create;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JobRunnerTest {

    @Test
    public void shouldExecuteJob() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobRunner jobRunner = newJobRunner(new JobInfo(() -> "NAME", jobUri), repository);
        // when
        jobRunner.startAsync(new JobRunnable() {
            @Override
            public JobType getJobType() {
                return () -> "NAME";
            }

            @Override
            public void run() {
            }
        });
        // then
        final JobInfo jobInfo = repository.findBy(jobUri).get();
        assertThat(jobInfo.getStatus(), is(OK));
        assertThat(jobInfo.getState(), is(STOPPED));
    }

    @Test
    public void shouldPersistJob() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobRunner jobRunner = newJobRunner(new JobInfo(() -> "NAME", jobUri), repository);
        // when
        jobRunner.startAsync(new JobRunnable() {
            @Override
            public JobType getJobType() {
                return () -> "NAME";
            }

            @Override
            public void run() {
            }
        });
        // then
        final Optional<JobInfo> optionalJob = repository.findBy(jobUri);
        assertThat(optionalJob, isPresent());
    }
}