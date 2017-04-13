package de.otto.edison.metrics.http;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import de.otto.edison.acceptance.api.StatusApi;
import de.otto.edison.testsupport.dsl.Then;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static de.otto.edison.acceptance.api.StatusApi.internal_status_is_retrieved_as;
import static de.otto.edison.acceptance.api.StatusApi.the_returned_content;
import static de.otto.edison.acceptance.api.StatusApi.the_status_code;
import static de.otto.edison.testsupport.dsl.Then.then;
import static de.otto.edison.testsupport.dsl.When.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

public class MetricsIntegrationTest {

    @Test
    public void shouldReportHttpCountAndTimeToGraphite() throws Exception {
        MetricRegistry metricRegistry = StatusApi.applicationContext().getBean(MetricRegistry.class);
        long counterBefore = Optional.ofNullable(metricRegistry.getCounters().get("counter.http.get.200")).orElse(new Counter()).getCount();

        //when
        internal_status_is_retrieved_as("text/html");

        //then
        long counterAfter = metricRegistry.getCounters().get("counter.http.get.200").getCount();
        assertThat(counterAfter - counterBefore).isEqualTo(1);
        long timerValues = metricRegistry.getTimers().get("timer.http.get").getSnapshot().size();
        assertThat(timerValues).isEqualTo(1);
    }

}
