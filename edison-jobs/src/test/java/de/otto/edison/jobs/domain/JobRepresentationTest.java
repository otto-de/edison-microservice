package de.otto.edison.jobs.domain;

import de.otto.edison.jobs.controller.JobRepresentation;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static de.otto.edison.jobs.controller.JobRepresentation.representationOf;
import static de.otto.edison.jobs.domain.JobInfo.JobStatus.OK;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static java.time.Clock.fixed;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JobRepresentationTest {

    @Test()
    public void shouldCalculateRuntime() throws InterruptedException {
        final JobInfo job = jobInfoWithRuntime(90, ChronoUnit.SECONDS);

        final JobRepresentation jobRepresentation = representationOf(job, null, true, "", "");

        assertThat(jobRepresentation.getStatus(), is("OK"));
        assertThat(jobRepresentation.getRuntime(), is("00:01:30"));
        assertThat(jobRepresentation.getHostname(), is("localhost"));
    }

    @Test
    public void shouldFormatRuntimeBiggerThan24Hours() throws Exception {
        final JobInfo job = jobInfoWithRuntime(25, ChronoUnit.HOURS);

        JobRepresentation jobRepresentation = representationOf(job, null, true, "", "");

        assertThat(jobRepresentation.getRuntime(), is("> 24h"));
    }

    @Test
    public void shouldFormatRuntimeLessThan24Hours() throws Exception {
        final JobInfo job = jobInfoWithRuntime(23, ChronoUnit.HOURS);

        JobRepresentation jobRepresentation = representationOf(job, null, true, "", "");

        assertThat(jobRepresentation.getRuntime(), is("23:00:00"));
    }

    private JobInfo jobInfoWithRuntime(int finishAmount, ChronoUnit unit) {
        final Clock clock = fixed(Instant.now(), systemDefault());
        final OffsetDateTime startTime = now(clock);
        final OffsetDateTime finishedTime = startTime.plus(finishAmount, unit);
        return newJobInfo("foo", "TEST", startTime, finishedTime, of(finishedTime), OK, emptyList(), clock, "localhost");
    }
}
