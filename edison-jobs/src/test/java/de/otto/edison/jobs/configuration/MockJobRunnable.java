package de.otto.edison.jobs.configuration;

import de.otto.edison.jobs.definition.DefaultJobDefinition;
import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.service.JobRunnable;

import java.util.Optional;

public class MockJobRunnable implements JobRunnable {

    public static final String MOCK_JOB_TYPE = "mockJobType";
    public static final String MOCK_JOB_NAME = "Mock Job name";

    @Override
    public JobDefinition getJobDefinition() {
        return DefaultJobDefinition.manuallyTriggerableJobDefinition(
                MOCK_JOB_TYPE,
                MOCK_JOB_NAME,
                "A mock job for testing purposes",
                1,
                Optional.empty()
        );
    }
}
