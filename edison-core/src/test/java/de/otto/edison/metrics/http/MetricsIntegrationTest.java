package de.otto.edison.metrics.http;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import de.otto.edison.acceptance.api.StatusApi;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static de.otto.edison.acceptance.api.StatusApi.internal_status_is_retrieved_as;
import static org.assertj.core.api.Assertions.assertThat;

public class MetricsIntegrationTest {

    private MetricRegistry metricRegistry;

    @Before
    public void setUp() throws Exception {
        metricRegistry = StatusApi.applicationContext().getBean(MetricRegistry.class);
    }

    @Test
    public void shouldReportHttpCountToGraphite() throws Exception {
        long counterBefore = Optional.ofNullable(metricRegistry.getCounters().get("counter.http.get.200")).orElse(new Counter()).getCount();

        //when
        internal_status_is_retrieved_as("text/html");

        //then
        long counterAfter = metricRegistry.getCounters().get("counter.http.get.200").getCount();
        assertThat(counterAfter - counterBefore).isEqualTo(1);
    }

    @Test
    public void shouldReportHttpTimeToGraphite() throws Exception {
        long timerSnapshotSizeBefore = Optional.ofNullable(metricRegistry.getTimers().get("timer.http.get")).orElse(new Timer()).getSnapshot().size();

        //when
        internal_status_is_retrieved_as("text/html");

        //then
        long timerSnapshotSizeAfter = metricRegistry.getTimers().get("timer.http.get").getSnapshot().size();
        assertThat(timerSnapshotSizeAfter - timerSnapshotSizeBefore).isEqualTo(1);
    }

}
