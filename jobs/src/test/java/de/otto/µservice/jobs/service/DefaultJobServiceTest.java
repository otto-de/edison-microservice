package de.otto.µservice.jobs.service;

import de.otto.µservice.jobs.domain.JobInfo;
import de.otto.µservice.jobs.repository.InMemJobRepository;
import org.testng.annotations.Test;

import java.net.URI;

import static de.otto.µservice.jobs.domain.JobInfo.ExecutionState.STOPPED;
import static de.otto.µservice.testsupport.matcher.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultJobServiceTest {

    @Test
    public void shouldReturnCreatedJobUri() {
        final DefaultJobService jobService = new DefaultJobService(new JobFactory("/foo"), new InMemJobRepository());
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobType()).thenReturn(() -> "BAR");
        final URI jobUri = jobService.startAsyncJob(jobRunnable);
        assertThat(jobUri.toString(), startsWith("/foo/jobs/"));
    }

    @Test
    public void shouldPersistJobs() {
        final InMemJobRepository jobRepository = new InMemJobRepository();
        final DefaultJobService jobService = new DefaultJobService(new JobFactory("/foo"), jobRepository);
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobType()).thenReturn(() -> "BAR");
        final URI jobUri = jobService.startAsyncJob(jobRunnable);
        assertThat(jobRepository.findBy(jobUri), isPresent());
    }

    @Test
    public void shouldRunJobs() {
        final InMemJobRepository jobRepository = new InMemJobRepository();
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobType()).thenReturn(() -> "BAR");
        final DefaultJobService jobService = new DefaultJobService(new JobFactory("/foo"), jobRepository);
        final URI jobUri = jobService.startAsyncJob(jobRunnable);
        final JobInfo jobInfo = jobRepository.findBy(jobUri).get();
        assertThat(jobInfo.getState(), is(STOPPED));
    }
}