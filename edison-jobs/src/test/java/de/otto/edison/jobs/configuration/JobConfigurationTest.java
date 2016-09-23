package de.otto.edison.jobs.configuration;

import org.junit.Test;

public class JobConfigurationTest {


    @Test(expected = IllegalArgumentException.class)
    public void shouldErrorOnWrongStatusMapping() {
        new JobConfiguration().parseStatusMapping("wrong");
    }
}