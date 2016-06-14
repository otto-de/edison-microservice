package de.otto.edison.jobs.service;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JobMutexHandlerTest {

    private InMemoryJobRunLockProvider jobRunLockProvider;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        jobRunLockProvider = new InMemoryJobRunLockProvider();
    }

    @Test
    public void singleJobShouldBeStartable() {
        // given
        JobMutexGroup jobMutexGroupA = new JobMutexGroup("groupA", "JobA", "JobB");
        JobMutexHandler jobMutexHandler = new JobMutexHandler(singleton(jobMutexGroupA), jobRunLockProvider);

        // when
        final boolean isJobStartable = jobMutexHandler.isJobStartable("JobA");

        // then
        assertThat(isJobStartable, is(true));
    }

    @Test
    public void jobShouldNotBeStartableTwice() {
        // given
        JobMutexHandler jobMutexHandler = new JobMutexHandler(Collections.emptySet(), jobRunLockProvider);

        // when
        final boolean isJobStartable = jobMutexHandler.isJobStartable("JobA");

        // then
        assertThat(isJobStartable, is(true));

        // when
        final boolean isJobStartable2 = jobMutexHandler.isJobStartable("JobA");

        // then
        assertThat(isJobStartable2, is(false));
    }

    @Test
    public void jobShouldBeStartableTwiceIfFinishedInBetween() {
        // given
        JobMutexHandler jobMutexHandler = new JobMutexHandler(Collections.emptySet(), jobRunLockProvider);

        // when
        final boolean isJobStartable = jobMutexHandler.isJobStartable("JobA");

        // then
        assertThat(isJobStartable, is(true));

        // when
        jobMutexHandler.jobHasStopped("JobA");
        final boolean isJobStartable2 = jobMutexHandler.isJobStartable("JobA");

        // then
        assertThat(isJobStartable2, is(true));
    }

    @Test
    public void jobShouldNotBeStartableIfOtherJobInMutexGroupIsRunning() {
        // given
        JobMutexGroup jobMutexGroupA = new JobMutexGroup("groupA", "JobA", "JobB");
        JobMutexHandler jobMutexHandler = new JobMutexHandler(singleton(jobMutexGroupA), jobRunLockProvider);

        // when
        final boolean isJobStartable = jobMutexHandler.isJobStartable("JobA");

        // then
        assertThat(isJobStartable, is(true));

        // when
        final boolean isJobStartable2 = jobMutexHandler.isJobStartable("JobB");

        // then
        assertThat(isJobStartable2, is(false));
    }

    @Test
    public void jobShouldBeStartableIfOtherJobInOverlappingMutexGroupIsRunning() {
        // given
        JobMutexGroup jobMutexGroupA = new JobMutexGroup("groupA", "JobA", "JobB");
        JobMutexGroup jobMutexGroupB = new JobMutexGroup("groupA", "JobC", "JobB");
        JobMutexHandler jobMutexHandler = new JobMutexHandler(new HashSet<>(asList(jobMutexGroupA, jobMutexGroupB)), jobRunLockProvider);

        // when
        final boolean isJobStartable = jobMutexHandler.isJobStartable("JobA");

        // then
        assertThat(isJobStartable, is(true));

        // when
        final boolean isJobStartable2 = jobMutexHandler.isJobStartable("JobC");

        // then
        assertThat(isJobStartable2, is(true));
    }




}