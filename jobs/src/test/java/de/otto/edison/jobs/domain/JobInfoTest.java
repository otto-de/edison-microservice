package de.otto.edison.jobs.domain;

import de.otto.edison.jobs.monitor.JobMonitor;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.DEAD;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.ERROR;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isAbsent;
import static java.net.URI.create;
import static java.time.Clock.fixed;
import static java.time.Clock.systemDefaultZone;
import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JobInfoTest {

    @Test
    public void shouldUpdateJobInfoInConstructor() {
        // given
        final URI jobUri = create("/foo/jobs/42");
        // when
        final JobMonitor monitor = mock(JobMonitor.class);
        final JobInfo jobInfo = newJobInfo(create("foo"), "TEST", monitor, systemDefaultZone());
        // then
        verify(monitor).update(jobInfo);
    }

    @Test
    public void shouldInitializeCorrectly() {
        final Clock clock = fixed(now(), systemDefault());
        final JobInfo job = newJobInfo(create("foo"), "TEST", mock(JobMonitor.class), clock);
        assertThat(job.getStatus(), is(OK));
        assertThat(job.getJobUri(), is(create("foo")));
        assertThat(job.getJobType(), is("TEST"));
        assertThat(job.getStarted().toInstant(), is(clock.instant()));
        assertThat(job.getStopped(), isAbsent());
        assertThat(job.getMessages(), hasSize(1));
    }


    @Test
    public void shouldStopAJob() {
        final Clock clock = fixed(now(), systemDefault());
        JobMonitor monitor = mock(JobMonitor.class);
        final JobInfo job = newJobInfo(create("foo"), "TEST", monitor, clock).stop();

        assertThat(job.isStopped(), is(true));
        assertThat(job.getStatus(), is(OK));
        verify(monitor, times(2)).update(any(JobInfo.class));
    }

    @Test
    public void shouldMarkAsError() {
        final Clock clock = fixed(now(), systemDefault());
        JobMonitor monitor = mock(JobMonitor.class);
        final JobInfo job = newJobInfo(create("foo"), "TEST", monitor, clock);
        job.info("first");
        job.error("BUMMMMMM");
        job.info("last");
        job.stop();
        assertThat(job.isStopped(), is(true));
        assertThat(job.getStatus(), is(ERROR));
        verify(monitor, times(5)).update(any(JobInfo.class));
    }

    @Test
    public void shouldMarkAsOkAfterRestart() {
        final Clock clock = fixed(now(), systemDefault());
        JobMonitor monitor = mock(JobMonitor.class);
        final JobInfo job = newJobInfo(create("foo"), "TEST", monitor, clock);
        job.error("BUMMMMMM");
        job.restart();
        job.stop();
        assertThat(job.isStopped(), is(true));
        assertThat(job.getStatus(), is(OK));
        verify(monitor, times(4)).update(any(JobInfo.class));
    }

    @Test
    public void shouldMarkAsDead() {
        final Clock clock = fixed(now(), systemDefault());
        final JobMonitor monitor = mock(JobMonitor.class);
        final JobInfo job = newJobInfo(create("foo"), "TEST", monitor, clock).dead();

        assertThat(job.isStopped(), is(true));
        assertThat(job.getStatus(), is(DEAD));
        assertThat(job.getStopped().get(), is(OffsetDateTime.now(clock)));
        verify(monitor, times(2)).update(any(JobInfo.class));
    }

    @Test
    public void shouldNotBeStopped() {
        final Clock clock = fixed(now(), systemDefault());
        JobMonitor monitor = mock(JobMonitor.class);
        final JobInfo job = newJobInfo(create("foo"), "TEST", monitor, clock);

        assertThat(job.isStopped(),is(false));
        assertThat(job.getStopped().isPresent(), is(false));
        verify(monitor, times(1)).update(job);
    }
}