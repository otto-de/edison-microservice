package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@SpringBootTest(classes = {SystemInfoConfiguration.class, PropertyPlaceholderAutoConfiguration.class,
        ApplicationInfoConfiguration.class, VersionInfoConfiguration.class, TeamInfoConfiguration.class,
        ApplicationStatusAggregatorConfiguration.class})
public class ApplicationStatusAggregatorConfigurationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ApplicationStatusAggregator applicationStatusAggregator;

    private ApplicationStatus status;

    @BeforeMethod
    public void setUp() {
        status = applicationStatusAggregator.aggregatedStatus();
    }

    @Test
    public void checkOverallStatus() {
        assertEquals(status.status, Status.OK);
        assertNotNull(status.application);
        assertNotNull(status.system);
        assertNotNull(status.vcs);
        assertNotNull(status.team);
        assertTrue(status.statusDetails.isEmpty());
        assertTrue(status.serviceSpecs.isEmpty());
    }

}