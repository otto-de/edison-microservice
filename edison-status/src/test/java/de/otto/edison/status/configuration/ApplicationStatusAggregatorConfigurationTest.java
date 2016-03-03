package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import de.otto.edison.status.indicator.load.LoadDetector;
import de.otto.edison.status.indicator.load.LoadStatusIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SpringApplicationConfiguration(classes = {SystemInfoConfiguration.class, PropertyPlaceholderAutoConfiguration.class,
        ApplicationInfoConfiguration.class, VersionInfoConfiguration.class, TeamInfoConfiguration.class,
        LoadStatusIndicator.class,
        ApplicationStatusAggregatorConfiguration.class, LoadIndicatorConfiguration.class})
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
    }

    @Test
    public void checkLoadStatus() {
        Optional<StatusDetail> loadDetail = status.statusDetails.stream().filter(detail -> detail.getName().equals("load")).findFirst();
        assertTrue(loadDetail.isPresent());
        assertEquals(loadDetail.get().getStatus(), Status.OK);
        assertEquals(loadDetail.get().getDetails().get("detail"), LoadDetector.Status.BALANCED.name());
    }

}