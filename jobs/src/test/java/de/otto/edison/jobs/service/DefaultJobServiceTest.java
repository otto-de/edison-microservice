package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.InMemJobRepository;
import de.otto.edison.jobs.repository.JobRepository;
import org.mockito.stubbing.Stubber;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.actuate.metrics.writer.DefaultGaugeService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.concurrent.ExecutorService;

import static de.otto.edison.jobs.domain.JobInfo.ExecutionState.STOPPED;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DefaultJobServiceTest {

    @Test
    public void shouldReturnCreatedJobUri() {
        // given:
        final DefaultJobService jobService = new DefaultJobService("/foo", new InMemJobRepository(), mock(GaugeService.class), mock(Clock.class));
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobType()).thenReturn(() -> "BAR");
        // when:
        final URI jobUri = jobService.startAsyncJob(jobRunnable);
        // then:
        assertThat(jobUri.toString(), startsWith("/foo/jobs/"));
    }

    @Test
    public void shouldPersistJobs() {
        // given:
        final InMemJobRepository jobRepository = new InMemJobRepository();
        final DefaultJobService jobService = new DefaultJobService("/foo", jobRepository, mock(GaugeService.class), mock(Clock.class));
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobType()).thenReturn(() -> "BAR");
        // when:
        final URI jobUri = jobService.startAsyncJob(jobRunnable);
        // then:
        assertThat(jobRepository.findBy(jobUri), isPresent());
    }

    @Test
    public void shouldRunJobs() {
        // given:
        final InMemJobRepository jobRepository = new InMemJobRepository();
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobType()).thenReturn(() -> "BAR");
        final DefaultJobService jobService = new DefaultJobService("/foo", jobRepository, mock(GaugeService.class), mock(Clock.class));
        // when:
        final URI jobUri = jobService.startAsyncJob(jobRunnable);
        // then:
        final JobInfo jobInfo = jobRepository.findBy(jobUri).get();
        assertThat(jobInfo.getState(), is(STOPPED));
    }

    @Test
    public void shouldReportRuntime() {
        // given:
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobType()).thenReturn(() -> "BAR");

        final GaugeService mock = mock(GaugeService.class);
        final DefaultJobService jobService = new DefaultJobService("/foo", mock(JobRepository.class), mock,mock(Clock.class));
        // when:
        jobService.startAsyncJob(jobRunnable);
        // then:
        verify(mock).submit(eq("gauge.jobs.runtime.bar"), anyLong());
    }

}