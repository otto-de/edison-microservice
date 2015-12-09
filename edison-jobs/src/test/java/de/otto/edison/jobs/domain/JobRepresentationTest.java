package de.otto.edison.jobs.domain;

import static de.otto.edison.jobs.controller.JobRepresentation.representationOf;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static java.net.URI.create;
import static java.time.Clock.fixed;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import org.testng.annotations.Test;

import de.otto.edison.jobs.controller.JobRepresentation;
import de.otto.edison.jobs.monitor.JobMonitor;

public class JobRepresentationTest {

    @Test()
    public void shouldCalculateRuntime() throws InterruptedException {
        final Clock clock = fixed(Instant.now(), systemDefault());
        final OffsetDateTime finishedTime = now(clock).plus(90, ChronoUnit.SECONDS);
        JobMonitor monitor = mock(JobMonitor.class);
        final JobInfo job = newJobInfo(create("foo"), "TEST", now(clock), finishedTime, of(finishedTime), OK, emptyList(), monitor, clock);

        final JobRepresentation jobRepresentation = representationOf(job, true, "");
        assertThat(jobRepresentation.getStatus(), is("OK"));
        assertThat(jobRepresentation.getRuntime(), is("00:01:30"));
       
    }
}