package de.otto.edison.jobs.configuration;


import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

public class JobsPropertiesTest {

    @Test
    public void shouldNormalizeJobTypes() throws Exception {
        // given
        final JobsProperties jobsProperties = new JobsProperties();
        jobsProperties.getStatus().setCalculator(new HashMap<String,String>() {{
            put("Some Job Type", "foo");
        }});
        // when
        Map<String, String> calculators = jobsProperties.getStatus().getCalculator();
        // then
        assertThat(calculators, hasEntry("some-job-type", "foo"));
    }
}