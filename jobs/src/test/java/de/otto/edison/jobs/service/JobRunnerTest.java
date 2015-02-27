package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfoBuilder;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.JobType;
import de.otto.edison.jobs.repository.InMemJobRepository;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Optional;

import static de.otto.edison.jobs.domain.JobInfo.ExecutionState.STOPPED;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.domain.Level.INFO;
import static de.otto.edison.jobs.service.JobRunner.newJobRunner;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isPresent;
import static java.net.URI.create;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class JobRunnerTest {

    @Test
    public void shouldExecuteJob() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobRunner jobRunner = newJobRunner(jobInfoBuilder(()->"NAME", jobUri).build(), repository);
        // when
        jobRunner.start(new JobRunnable() {
            @Override
            public JobType getJobType() {
                return () -> "NAME";
            }

            @Override
            public void execute(JobLogger logger) {
            }
        });
        // then
        final JobInfo jobInfo = repository.findBy(jobUri).get();
        assertThat(jobInfo.getStatus(), is(OK));
        assertThat(jobInfo.getState(), is(STOPPED));
    }

    @Test
    public void shouldPersistJobInfo() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobRunner jobRunner = newJobRunner(jobInfoBuilder(() -> "NAME", jobUri).build(), repository);
        // when
        jobRunner.start(new JobRunnable() {
            @Override
            public JobType getJobType() {
                return () -> "NAME";
            }

            @Override
            public void execute(JobLogger logger) {
            }
        });
        // then
        final Optional<JobInfo> optionalJob = repository.findBy(jobUri);
        assertThat(optionalJob, isPresent());
    }

    @Test
    public void shouldAddMessageToJobInfo() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        final InMemJobRepository repository = new InMemJobRepository();
        final JobRunner jobRunner = newJobRunner(jobInfoBuilder(() -> "NAME", jobUri).build(), repository);
        // when
        jobRunner.start(new JobRunnable() {
            @Override
            public JobType getJobType() {
                return () -> "NAME";
            }

            @Override
            public void execute(final JobLogger logger) {
                logger.log(jobMessage(INFO, "a message"));
            }
        });
        // then
        final Optional<JobInfo> optionalJob = repository.findBy(jobUri);
        final JobInfo jobInfo = optionalJob.get();
        assertThat(jobInfo.getMessages(), hasSize(1));
        final JobMessage message = jobInfo.getMessages().get(0);
        assertThat(message.getMessage(), is("a message"));
        assertThat(message.getLevel(), is(INFO));
        assertThat(message.getTimestamp(), is(notNullValue()));
    }
}