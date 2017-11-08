package de.otto.edison.jobs.status;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.DEAD;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.ERROR;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.SKIPPED;
import static de.otto.edison.jobs.status.JobStatusCalculator.errorOnLastJobFailed;
import static de.otto.edison.jobs.status.JobStatusCalculator.errorOnLastNumJobsFailed;
import static de.otto.edison.jobs.status.JobStatusCalculator.warningOnLastJobFailed;
import static java.time.Duration.ofSeconds;
import static java.time.OffsetDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobStatusCalculatorTest {

    private final JobDefinition jobDefinition = fixedDelayJobDefinition(
            "test",
            "test",
            "",
            ofSeconds(10),
            0,
            of(ofSeconds(10))
    );
    private JobRepository jobRepository;
    private JobStatusCalculator warningOnLastJobFailed;
    private JobStatusCalculator errorOnLastJobFailed;
    private JobStatusCalculator errorOnLastTwoJobsFailed;

    @Before
    public void setUp() throws Exception {
        jobRepository = mock(JobRepository.class);
        warningOnLastJobFailed = warningOnLastJobFailed("test", jobRepository, "/someInternalPath");
        errorOnLastJobFailed = errorOnLastJobFailed("test", jobRepository, "/someInternalPath");
        errorOnLastTwoJobsFailed = errorOnLastNumJobsFailed("test", 2, jobRepository, "/someInternalPath");
    }

    @Test
    public void shouldIndicateOkIfLastJobOk() {
        // given
        final List<JobInfo> jobs = singletonList(someStoppedJob(OK, 1));
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(jobs);

        // when
        StatusDetail first = errorOnLastJobFailed.statusDetail(jobDefinition);
        StatusDetail second = warningOnLastJobFailed.statusDetail(jobDefinition);

        // then
        assertThat(first.getStatus(), is(Status.OK));
        assertThat(first.getMessage(), is("Last job was successful"));
        assertThat(second.getStatus(), is(Status.OK));
    }

    @Test
    public void shouldIndicateOkIfLastJobSkipped() {
        // given
        final List<JobInfo> jobs = singletonList(someStoppedJob(SKIPPED, 1));
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(jobs);

        // when
        StatusDetail first = errorOnLastJobFailed.statusDetail(jobDefinition);
        StatusDetail second = warningOnLastJobFailed.statusDetail(jobDefinition);

        // then
        assertThat(first.getStatus(), is(Status.OK));
        assertThat(first.getMessage(), is("Last job was successful"));
        assertThat(second.getStatus(), is(Status.OK));
    }

    @Test
    public void shouldReturnStatusDetailWithJobLink() {
        // given
        final JobInfo jobInfo = someStoppedJob(OK, 1);
        final List<JobInfo> jobs = singletonList(jobInfo);
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(jobs);

        // when
        StatusDetail first = errorOnLastJobFailed.statusDetail(jobDefinition);

        // then
        assertThat(first.getLinks().size(), is(1));
        assertThat(first.getLinks().get(0).href, is("/someInternalPath/jobs/" + jobInfo.getJobId()));
    }

    @Test
    public void shouldIndicateStateIfLastJobFailed() {
        // given
        final List<JobInfo> jobInfos = singletonList(someStoppedJob(ERROR, 1));
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(jobInfos);

        // when
        StatusDetail first = errorOnLastJobFailed.statusDetail(jobDefinition);
        StatusDetail second = warningOnLastJobFailed.statusDetail(jobDefinition);

        // then
        assertThat(first.getStatus(), is(Status.ERROR));
        assertThat(first.getMessage(), is("Job had an error"));
        assertThat(second.getStatus(), is(Status.WARNING));
        assertThat(second.getMessage(), is("Job had an error"));
    }

    @Test
    public void shouldIndicateOkIfLastOfTwoJobsOk() {
        // given
        final List<JobInfo> jobInfos = asList(
                someStoppedJob(OK, 1),
                someStoppedJob(ERROR, 2));
        when(jobRepository.findLatestBy(anyString(), eq(2))).thenReturn(jobInfos);

        // when
        StatusDetail detail = errorOnLastTwoJobsFailed.statusDetail(jobDefinition);

        // then
        assertThat(detail.getStatus(), is(Status.OK));
        assertThat(detail.getMessage(), is("Last job was successful"));
    }

    @Test
    public void shouldIndicateWarningIfOneOfTwoJobsOk() {
        // given
        final List<JobInfo> jobInfos = asList(
                someStoppedJob(ERROR, 1),
                someStoppedJob(OK, 2));
        when(jobRepository.findLatestBy(anyString(), eq(2))).thenReturn(jobInfos);

        // when
        StatusDetail detail = errorOnLastTwoJobsFailed.statusDetail(jobDefinition);

        // then
        assertThat(detail.getStatus(), is(Status.WARNING));
        assertThat(detail.getMessage(), is("1 out of 2 job executions failed"));
    }

    @Test
    public void shouldIndicateWarningIfTwoOfThreeJobsFailed() {
        // given
        final List<JobInfo> jobInfos = asList(
                someStoppedJob(OK, 1),
                someStoppedJob(ERROR, 2),
                someStoppedJob(ERROR, 2));
        when(jobRepository.findLatestBy(anyString(), eq(3))).thenReturn(jobInfos);

        // when
        JobStatusCalculator maxOneOfThree = new JobStatusCalculator("test", 3, 1, jobRepository, "/someInternalPath");
        StatusDetail detail = maxOneOfThree.statusDetail(jobDefinition);

        // then
        assertThat(detail.getStatus(), is(Status.WARNING));
        assertThat(detail.getMessage(), is("2 out of 3 job executions failed"));
    }

    @Test
    public void shouldIndicateErrorIfTwoOfThreeJobsFailed() {
        // given
        final List<JobInfo> jobInfos = asList(
                someStoppedJob(ERROR, 1),
                someStoppedJob(OK, 2),
                someStoppedJob(ERROR, 2));
        when(jobRepository.findLatestBy(anyString(), eq(3))).thenReturn(jobInfos);

        // when
        JobStatusCalculator maxOneOfThree = new JobStatusCalculator("test", 3, 1, jobRepository, "/someInternalPath");
        StatusDetail detail = maxOneOfThree.statusDetail(jobDefinition);

        // then
        assertThat(detail.getStatus(), is(Status.ERROR));
        assertThat(detail.getMessage(), is("2 out of 3 job executions failed"));
    }

    @Test
    public void shouldIndicateErrorIfTwoJobsFailed() {
        // given
        final List<JobInfo> jobInfos = asList(
                someStoppedJob(ERROR, 1),
                someStoppedJob(ERROR, 2));
        when(jobRepository.findLatestBy(anyString(), eq(2))).thenReturn(jobInfos);

        // when
        StatusDetail detail = errorOnLastTwoJobsFailed.statusDetail(jobDefinition);

        // then
        assertThat(detail.getStatus(), is(Status.ERROR));
        assertThat(detail.getMessage(), is("2 out of 2 job executions failed"));
    }

    @Test
    public void shouldIndicateWarningIfLastJobRunWasTooLongAgo() {
        // given
        final List<JobInfo> jobInfo = singletonList(someStoppedJob(OK, 11));
        final List<JobInfo> jobInfos = asList(
                someStoppedJob(OK, 11),
                someStoppedJob(OK, 12));
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(jobInfo);
        when(jobRepository.findLatestBy(anyString(), eq(2))).thenReturn(jobInfos);

        // when
        StatusDetail first = errorOnLastJobFailed.statusDetail(jobDefinition);
        StatusDetail second = warningOnLastJobFailed.statusDetail(jobDefinition);
        StatusDetail third = errorOnLastTwoJobsFailed.statusDetail(jobDefinition);

        // then
        assertThat(first.getStatus(), is(Status.WARNING));
        assertThat(first.getMessage(), is("Job didn't run in the past 10 seconds"));
        assertThat(second.getStatus(), is(Status.WARNING));
        assertThat(second.getMessage(), is("Job didn't run in the past 10 seconds"));
        assertThat(third.getStatus(), is(Status.WARNING));
        assertThat(third.getMessage(), is("Job didn't run in the past 10 seconds"));
    }

    @Test
    public void shouldNotHaveUriOrRunningIfNoJobPresent() {
        // given
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(emptyList());

        // when
        StatusDetail statusDetail = errorOnLastJobFailed.statusDetail(jobDefinition);

        // then
        assertThat(statusDetail.getLinks(), is(emptyList()));
        assertThat(statusDetail.getDetails(), not(hasKey("Running")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldIndicateThatJobIsNotRunning() {
        // given
        final List<JobInfo> jobInfos = singletonList(someStoppedJob(OK, 1));
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(jobInfos);

        // when
        StatusDetail statusDetail = errorOnLastJobFailed.statusDetail(jobDefinition);

        // then
        assertThat(statusDetail.getDetails(), not(hasKey("Running")));
        assertThat(statusDetail.getDetails(), hasKey("Stopped"));
    }

    @Test
    public void shouldIndicateWarningIfJobRunWasDead() {
        // given
        final List<JobInfo> jobInfos = singletonList(someRunningJob(DEAD, 1));
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(jobInfos);

        // when
        StatusDetail statusDetail = errorOnLastJobFailed.statusDetail(jobDefinition);

        // then
        assertThat(statusDetail.getStatus(), is(Status.WARNING));
        assertThat(statusDetail.getMessage(), is("Job died"));
    }

    @Test
    public void shouldIndicateWarningIfLastJobRunWasDead() {
        // given
        final List<JobInfo> jobInfos = singletonList(someStoppedJob(DEAD, 1));
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(jobInfos);

        // when
        StatusDetail statusDetail = errorOnLastJobFailed.statusDetail(jobDefinition);

        // then
        assertThat(statusDetail.getStatus(), is(Status.WARNING));
        assertThat(statusDetail.getMessage(), is("Job died"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldIndicateErrorIfJobCouldNotBeRetievedFromRepository() {
        // given
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenThrow(RuntimeException.class);

        // when
        StatusDetail statusDetail = errorOnLastJobFailed.statusDetail(jobDefinition);

        // then
        assertThat(statusDetail.getStatus(), is(Status.ERROR));
    }

    @Test
    public void shouldAcceptIfNoJobRan() {
        // given
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(emptyList());

        // when
        StatusDetail statusDetail = errorOnLastJobFailed.statusDetail(jobDefinition);

        // then
        assertThat(statusDetail.getStatus(), is(Status.OK));
    }

    @Test
    public void shouldHaveName() {
        // given
        final List<JobInfo> jobInfos = singletonList(someStoppedJob(OK, 1));
        when(jobRepository.findLatestBy(anyString(), eq(1))).thenReturn(jobInfos);

        // when
        StatusDetail statusDetail = errorOnLastJobFailed.statusDetail(jobDefinition);

        // then
        assertThat(statusDetail.getName(), is("test"));
    }

    private JobInfo someStoppedJob(final JobInfo.JobStatus jobStatus, int startedSecondsAgo) {
        OffsetDateTime now = now();
        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("someId");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(startedSecondsAgo));
        when(someJob.getStopped()).thenReturn(of(now.minusSeconds(startedSecondsAgo-1)));
        when(someJob.getStatus()).thenReturn(jobStatus);
        return someJob;
    }

    private JobInfo someRunningJob(final JobInfo.JobStatus jobStatus, int startedSecondsAgo) {
        OffsetDateTime now = now();
        JobInfo someJob = mock(JobInfo.class);
        when(someJob.getJobType()).thenReturn("someJobType");
        when(someJob.getJobId()).thenReturn("someJobId");
        when(someJob.getStarted()).thenReturn(now.minusSeconds(startedSecondsAgo));
        when(someJob.getStopped()).thenReturn(empty());
        when(someJob.getStatus()).thenReturn(jobStatus);
        return someJob;
    }
}
