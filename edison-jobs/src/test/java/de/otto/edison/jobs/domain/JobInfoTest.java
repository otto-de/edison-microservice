package de.otto.edison.jobs.domain;

import de.otto.edison.status.domain.SystemInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Clock;
import java.time.OffsetDateTime;

import static de.otto.edison.jobs.domain.JobInfo.JobStatus.*;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isAbsent;
import static java.net.URI.create;
import static java.time.Clock.fixed;
import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JobInfoTest {

    private Clock clock;
    private String hostname;

    @BeforeMethod
    public void setUp() {
        clock = fixed(now(), systemDefault());
        hostname = "localhost";
    }

    @Test
    public void shouldInitializeCorrectly() {
        JobInfo job = newJobInfo(create("foo"), "TEST", clock, "localhost");
        assertThat(job.getStatus(), is(OK));
        assertThat(job.getJobUri(), is(create("foo")));
        assertThat(job.getJobType(), is("TEST"));
        assertThat(job.getHostname(), is(hostname));
        assertThat(job.getStarted().toInstant(), is(clock.instant()));
        assertThat(job.getStopped(), isAbsent());
    }

    @Test
    public void shouldStopAJob() {
        JobInfo job = newJobInfo(create("foo"), "TEST", clock, "localhost").stop();

        assertThat(job.isStopped(), is(true));
        assertThat(job.getStatus(), is(OK));
    }

    @Test
    public void shouldMarkAsError() {
        JobInfo job = newJobInfo(create("foo"), "TEST", clock, "localhost");
        job.info("first");
        job.error("BUMMMMMM");
        job.info("last");
        job.stop();
        assertThat(job.isStopped(), is(true));
        assertThat(job.getStatus(), is(ERROR));
    }

    @Test
    public void shouldMarkAsOkAfterRestart() {
        JobInfo job = newJobInfo(create("foo"), "TEST", clock, "localhost");
        job.error("BUMMMMMM");
        job.restart();
        job.stop();
        assertThat(job.isStopped(), is(true));
        assertThat(job.getStatus(), is(OK));
    }

    @Test
    public void shouldMarkAsDead() {
        JobInfo job = newJobInfo(create("foo"), "TEST", clock, "localhost").dead();

        assertThat(job.isStopped(), is(true));
        assertThat(job.getStatus(), is(DEAD));
        assertThat(job.getStopped().get(), is(OffsetDateTime.now(clock)));
    }

    @Test
    public void shouldNotBeStopped() {
        JobInfo job = newJobInfo(create("foo"), "TEST", clock, "localhost");

        assertThat(job.isStopped(), is(false));
        assertThat(job.getStopped().isPresent(), is(false));
    }
}