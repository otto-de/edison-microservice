package de.otto.edison.jobs.configuration;

import de.otto.edison.jobs.repository.AggregateCleanupStrategy;
import de.otto.edison.jobs.repository.JobCleanupStrategy;
import de.otto.edison.jobs.repository.KeepLastJobs;
import de.otto.edison.jobs.repository.StopDeadJobs;
import de.otto.edison.testsupport.applicationdriver.SpringTestBase;
import org.hamcrest.Matcher;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class ConfigurationTest extends SpringTestBase {
    @Test
    public void shouldWireTheDefaultCleanupStrategies() throws Exception {
        assertThat(classesOfWiredCleanupStrategies(), is(singletonList(AggregateCleanupStrategy.class)));
    }

    private Collection<? extends Class> classesOfWiredCleanupStrategies() {
        Map<String, JobCleanupStrategy> beansOfType = applicationContext().getBeansOfType(JobCleanupStrategy.class);
        return beansOfType.values().stream().map(Object::getClass).collect(toList());
    }
}
