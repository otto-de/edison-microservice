package de.otto.edison.metrics.load;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static de.otto.edison.metrics.load.LoadDetector.Status.BALANCED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PropertyPlaceholderAutoConfiguration.class,
        MetricRegistry.class, MetricsLoadAutoConfiguration.class})
public class MetricsLoadAutoConfigurationTest {

    @Autowired
    private LoadDetector loadDetector;

    @Test
    public void checkOverallStatus() {
        assertThat(loadDetector.getStatus(), is(BALANCED));
    }

}