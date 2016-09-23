package de.otto.edison.jobs.status;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;

import static de.otto.edison.jobs.status.JobStatusDetailIndicator.ERROR_MESSAGE;
import static de.otto.edison.jobs.status.JobStatusDetailIndicator.SUCCESS_MESSAGE;
import static java.time.Duration.ofHours;
import static java.time.Duration.ofSeconds;
import static java.time.OffsetDateTime.now;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobStatusDetailIndicatorTest {

    private JobRepository jobRepository;

    @Before
    public void setUp() throws Exception {
        jobRepository = mock(JobRepository.class);
    }

    @Test
    public void shouldIndicateOkIfJobRunWasSuccessful() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/job/url");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(1));
        when(someJob.getStopped()).thenReturn(Optional.of(now));
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.OK);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getStatus(), is(Status.OK));
    }

    @Test
    public void shouldIndicateWarningIfLastJobRunWasTooLongAgo() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/job/url");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(21));
        when(someJob.getStopped()).thenReturn(Optional.of(now.minusSeconds(20)));
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.OK);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofSeconds(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getStatus(), is(Status.WARNING));
        assertThat(status.getMessage(), containsString("Job didn't run in the past"));
    }

    @Test
    public void shouldHaveOkMessage() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/job/url");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(1));
        when(someJob.getStopped()).thenReturn(Optional.empty());
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.OK);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getMessage(), is(SUCCESS_MESSAGE));
    }

    @Test
    public void shouldHaveUri() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobId()).thenReturn("/some/uri");
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(1));
        when(someJob.getStopped()).thenReturn(Optional.empty());
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.OK);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getDetails(), hasEntry("uri", "/some/uri"));
    }

    @Test
    public void shouldHaveUriWhenNoJobIsRunning() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/uri");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(1));
        when(someJob.getStopped()).thenReturn(Optional.of(now));
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.OK);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getDetails(), hasEntry("uri", "/some/uri"));
    }

    @Test
    public void shouldNotHaveUriOrRunningIfNoJobPresent() {
        // given
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList());

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getDetails(), not(hasKey("uri")));
        assertThat(status.getDetails(), not(hasKey("running")));
    }

    @Test
    public void shouldIndicateThatJobIsRunning() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/uri");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(1));
        when(someJob.getStopped()).thenReturn(Optional.empty());
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.OK);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getDetails(), hasEntry("running", "/some/uri"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldIndicateThatJobIsNotRunning() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/job/url");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(1));
        when(someJob.getStopped()).thenReturn(Optional.of(now));
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.OK);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getDetails(), not(hasKey("running")));
    }

    @Test
    public void shouldHaveErrorMessage() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/job/url");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(1));
        when(someJob.getStopped()).thenReturn(Optional.empty());
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.ERROR);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getMessage(), is(ERROR_MESSAGE));
    }

    @Test
    public void shouldIndicateConfiguredErrorStatusIfJobRunWasErrornous() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/job/url");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(1));
        when(someJob.getStopped()).thenReturn(Optional.empty());
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.ERROR);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getStatus(), is(Status.ERROR));
    }

    @Test
    public void shouldIndicateConfiguredWarningStatusIfJobRunWasErrornous() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/job/url");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(1));
        when(someJob.getStopped()).thenReturn(Optional.empty());
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.ERROR);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.WARNING);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getStatus(), is(Status.WARNING));
    }

    @Test
    public void shouldIndicateWarningIfJobRunWasDead() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/job/url");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(1));
        when(someJob.getStopped()).thenReturn(Optional.empty());
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.DEAD);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getStatus(), is(Status.WARNING));
    }

    @Test
    public void shouldIndicateWarningIfLastJobRunWasDead() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/job/url");
        when(someJob.getStarted()).thenReturn(now);
        when(someJob.getStopped()).thenReturn(Optional.empty());
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.DEAD);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getStatus(), is(Status.WARNING));
    }

    @Test
    public void shouldIndicateErrorIfJobCouldNotBeRetievedFromRepository() {
        // given
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenThrow(RuntimeException.class);

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getStatus(), is(Status.ERROR));
    }

    @Test
    public void shouldFilterByJobType() {
        // given
        OffsetDateTime now = now();

        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("/some/job/url");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(1));
        when(someJob.getStopped()).thenReturn(Optional.empty());
        when(someJob.getStatus()).thenReturn(JobInfo.JobStatus.ERROR);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(Arrays.asList(someJob));

        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType2", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getStatus(), is(Status.ERROR));
    }

    @Test
    public void shouldAcceptIfNoJobRan() {
        // given
        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);
        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getStatus(), is(Status.OK));
    }

    @Test
    public void shouldHaveName() {
        // given
        JobStatusDetailIndicator jobStatusDetailIndicator = new JobStatusDetailIndicator(jobRepository, "someName", "someJobType", Optional.of(ofHours(10)), Status.ERROR);

        // when
        StatusDetail status = jobStatusDetailIndicator.statusDetail();

        // then
        assertThat(status.getName(), is("someName"));
    }
}