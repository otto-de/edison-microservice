package de.otto.edison.jobs.service;


import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Test
public class InMemoryJobRunLockProviderTest {

    private InMemoryJobRunLockProvider inMemoryJobRunLockProvider;


    @BeforeMethod
    public void setUp() throws Exception {
        inMemoryJobRunLockProvider = new InMemoryJobRunLockProvider();
    }


    @Test
    public void shouldAcquireAllLocks() {
        // given
        Set<String> jobTypes = new HashSet<String>(asList("jobA", "jobB"));

        // when
        final boolean runLocksForJobTypes = inMemoryJobRunLockProvider.acquireRunLocksForJobTypes(jobTypes);

        // then
        assertThat(runLocksForJobTypes, is(true));
        assertThat (inMemoryJobRunLockProvider.locks, hasSize(2));
        assertThat (inMemoryJobRunLockProvider.locks.contains("jobA"), is(true));
        assertThat (inMemoryJobRunLockProvider.locks.contains("jobB"), is(true));
    }


    @Test
    public void shouldCanNotAcquireLockBecauseItIsAlreadyAcquired() {
        // given
        Set<String> jobTypes = new HashSet<String>(asList("jobA"));

        // when
        final boolean runLocksForJobTypes = inMemoryJobRunLockProvider.acquireRunLocksForJobTypes(jobTypes);
        final boolean runLocksForJobTypes2 = inMemoryJobRunLockProvider.acquireRunLocksForJobTypes(jobTypes);

        // then
        assertThat(runLocksForJobTypes, is(true));
        assertThat(runLocksForJobTypes2, is(false));

        assertThat (inMemoryJobRunLockProvider.locks, hasSize(1));
        assertThat (inMemoryJobRunLockProvider.locks.contains("jobA"), is(true));

    }



    @Test
    public void shouldDenyAcquiringLocksIfOneOfTheirMembersIsAlreadyAcquiredInAnotherLockGroup() {
        // given
        Set<String> jobTypes1 = new HashSet<>(asList("jobA","jobB"));
        Set<String> jobTypes2 = new HashSet<>(asList("jobA", "jobB", "jobC", "jobD"));

        // when
        final boolean runLocksForJobTypes = inMemoryJobRunLockProvider.acquireRunLocksForJobTypes(jobTypes1);

        // then
        assertThat(runLocksForJobTypes, is(true));
        assertThat (inMemoryJobRunLockProvider.locks, hasSize(2));
        assertThat (inMemoryJobRunLockProvider.locks.contains("jobA"), is(true));
        assertThat (inMemoryJobRunLockProvider.locks.contains("jobB"), is(true));

        // and when
        final boolean runLocksForJobTypes2 = inMemoryJobRunLockProvider.acquireRunLocksForJobTypes(jobTypes2);

        // then
        assertThat(runLocksForJobTypes2, is(false));
        assertThat (inMemoryJobRunLockProvider.locks, hasSize(2));
        assertThat (inMemoryJobRunLockProvider.locks.contains("jobC"), is(false));
        assertThat (inMemoryJobRunLockProvider.locks.contains("jobD"), is(false));
    }



    @Test
    public void shouldReleaseAcquireLocks() {
        // given
        Set<String> jobTypes = new HashSet<>(asList("jobA", "jobB", "jobC"));

        // when
        inMemoryJobRunLockProvider.acquireRunLocksForJobTypes(jobTypes);
        inMemoryJobRunLockProvider.releaseRunLocksForJobTypes(new HashSet<>(asList("jobB")));

        // then
        assertThat (inMemoryJobRunLockProvider.locks, hasSize(2));
        assertThat (inMemoryJobRunLockProvider.locks.contains("jobA"), is(true));
        assertThat (inMemoryJobRunLockProvider.locks.contains("jobC"), is(true));
    }

}