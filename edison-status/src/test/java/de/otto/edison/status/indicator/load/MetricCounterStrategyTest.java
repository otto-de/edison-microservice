package de.otto.edison.status.indicator.load;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
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
        initializeMetricRegistry(metricRegistry, 8);
        MetricCounterStrategy strategy = new MetricCounterStrategy(metricRegistry, "my.counter", 10, 25);
        assertThat(strategy.getStatus(), is(Status.IDLE));
    }

    @Test
    public void thatStatusBalance() throws Exception {
        initializeMetricRegistry(metricRegistry, 12);
        MetricCounterStrategy strategy = new MetricCounterStrategy(metricRegistry, "my.counter", 10, 25);
        assertThat(strategy.getStatus(), is(Status.BALANCED));
    }

    @Test
    public void thatStatusOverload() throws Exception {
        initializeMetricRegistry(metricRegistry, 42);
        MetricCounterStrategy strategy = new MetricCounterStrategy(metricRegistry, "my.counter", 10, 25);
        assertThat(strategy.getStatus(), is(Status.OVERLOAD));
    }

    // ~~

    private static void initializeMetricRegistry(MetricRegistry metricRegistry, long counterValue) {
        Counter counter = new Counter();
        counter.inc(counterValue);
        when(metricRegistry.getCounters(any())).thenReturn(new TreeMap<String, Counter>() {{
            put("my.counter", counter);
        }});
        when(metricRegistry.counter("my.counter")).thenReturn(counter);
    }

}