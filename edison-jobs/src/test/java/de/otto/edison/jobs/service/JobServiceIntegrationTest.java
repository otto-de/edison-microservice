package de.otto.edison.jobs.service;

import de.otto.edison.testsupport.TestServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

@SpringApplicationConfiguration(classes = TestServer.class)
public class JobServiceIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    JobService jobService;

    @Test
    public void shouldFindJobService() {
        assertThat(jobService, is(notNullValue()));
    }

}
