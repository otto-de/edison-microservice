package de.otto.edison.jobs.service;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.InMemJobRepository;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultJobServiceTest {

    private ExecutorService executorService;

    @BeforeMethod
    public void setUp() {
        executorService = mock(ExecutorService.class);
        immediatelyRunGivenRunnable()
                .when(executorService)
                .execute(any(Runnable.class));
    }

    @Test
    public void shouldReturnCreatedJobUri() {
        final DefaultJobService jobService = new DefaultJobService(new JobFactory("/foo"), new InMemJobRepository(),executorService);
        final JobRunnable jobRunnable = mock(JobRunnable.class);
        when(jobRunnable.getJobType()).thenReturn(() -> "BAR");
        final URI jobUri = jobService.startAsyncJob(jobRunnable);
        assertThat(jobUri.toString(), startsWith("/foo/jobs/"));
    }

    @Test
    public void shouldPersistJobs() {
        final InMemJobRepository jobRepository = new InMemJobRepository();
        final DefaultJobService jobService = new DefaultJobService(new JobFactory("/foo"), jobRepository,executorService);
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
        final DefaultJobService jobService = new DefaultJobService(new JobFactory("/foo"), jobRepository,executorService);
        final URI jobUri = jobService.startAsyncJob(jobRunnable);
        final JobInfo jobInfo = jobRepository.findBy(jobUri).get();
        assertThat(jobInfo.getState(), is(STOPPED));
    }

    private Stubber immediatelyRunGivenRunnable() {
        return doAnswer(i -> {
            ((Runnable) i.getArguments()[0]).run();
            return null;
        });
    }
}