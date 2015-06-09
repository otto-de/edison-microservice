package de.otto.edison.jobs.configuration;

import de.otto.edison.jobs.repository.JobCleanupStrategy;
import de.otto.edison.jobs.repository.KeepLastJobs;
import de.otto.edison.jobs.repository.StopDeadJobs;
import de.otto.edison.testsupport.applicationdriver.SpringTestBase;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;


public class ConfigurationTest extends SpringTestBase {
    @Test
    public void shouldWireTheDefaultCleanupStrategies() throws Exception {
        assertThat(classesOfWiredCleanupStrategies(), containsInAnyOrder(KeepLastJobs.class, StopDeadJobs.class));
    }

    private Collection<? extends Class> classesOfWiredCleanupStrategies() {
        Map<String, JobCleanupStrategy> beansOfType = applicationContext().getBeansOfType(JobCleanupStrategy.class);
        return beansOfType.values().stream().map(Object::getClass).collect(toList());
    }
}
