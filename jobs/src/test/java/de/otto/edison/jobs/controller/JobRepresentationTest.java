package de.otto.edison.jobs.controller;


import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfoBuilder;
import org.testng.annotations.Test;

import java.net.URI;

import static de.otto.edison.jobs.controller.JobRepresentation.representationOf;
import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;

public class JobRepresentationTest {
    @Test
    public void shouldBeAbleToDealWithJobsWithoutLastUpdatedTimestampForLegacyData() throws Exception {
        JobInfo someJob = JobInfoBuilder.jobInfoBuilder("TYPE", URI.create("some/uri"))
                .withLastUpdated(null)
                .build();
        JobRepresentation jobRepresentation = representationOf(someJob);

        assertThat(jobRepresentation.getLastUpdated(),is(nullValue()));

    }
}