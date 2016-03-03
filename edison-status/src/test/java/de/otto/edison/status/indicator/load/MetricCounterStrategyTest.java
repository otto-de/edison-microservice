package de.otto.edison.status.indicator.load;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import de.otto.edison.status.indicator.load.LoadDetector.Status;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricCounterStrategyTest {

    private MetricRegistry metricRegistry;

    @BeforeMethod
    public void setUp() {
        metricRegistry = mock(MetricRegistry.class);
    }

    @Test
    public void thatStatusIdle() throws Exception {
        initializeMetricRegistry(metricRegistry, 8.0d);
        MetricCounterStrategy strategy = new MetricCounterStrategy(metricRegistry, "my.counter", 10, 25);
        assertThat(strategy.getStatus(), is(Status.IDLE));
    }

    @Test
    public void thatStatusBalance() throws Exception {
        initializeMetricRegistry(metricRegistry, 12.0d);
        MetricCounterStrategy strategy = new MetricCounterStrategy(metricRegistry, "my.counter", 10, 25);
        assertThat(strategy.getStatus(), is(Status.BALANCED));
    }

    @Test
    public void thatStatusOverload() throws Exception {
        initializeMetricRegistry(metricRegistry, 42.0d);
        MetricCounterStrategy strategy = new MetricCounterStrategy(metricRegistry, "my.counter", 10, 25);
        assertThat(strategy.getStatus(), is(Status.OVERLOAD));
    }

    // ~~

    private static void initializeMetricRegistry(MetricRegistry metricRegistry, double counterValue) {
        Timer timer = mock(Timer.class);
        when(timer.getOneMinuteRate()).thenReturn(counterValue);
        when(metricRegistry.getTimers(any())).thenReturn(new TreeMap<String, Timer>() {{
            put("my.counter", timer);
        }});
        when(metricRegistry.timer("my.counter")).thenReturn(timer);
    }

}