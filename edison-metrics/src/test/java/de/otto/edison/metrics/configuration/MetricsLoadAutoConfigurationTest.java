package de.otto.edison.metrics.configuration;

import com.codahale.metrics.MetricRegistry;
import de.otto.edison.metrics.load.LoadDetector;
import de.otto.edison.metrics.load.LoadDetector.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

@SpringApplicationConfiguration(classes = {PropertyPlaceholderAutoConfiguration.class,
        MetricRegistry.class, MetricsLoadAutoConfiguration.class})
public class MetricsLoadAutoConfigurationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LoadDetector loadDetector;

    @Test
    public void checkOverallStatus() {
        assertEquals(loadDetector.getStatus(), Status.BALANCED);
    }

}