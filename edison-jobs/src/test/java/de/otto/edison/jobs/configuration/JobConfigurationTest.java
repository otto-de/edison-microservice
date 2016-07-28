package de.otto.edison.jobs.configuration;

import org.testng.annotations.Test;

public class JobConfigurationTest {


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldErrorOnWrongStatusMapping() {
        new JobConfiguration().parseStatusMapping("wrong");
    }
}