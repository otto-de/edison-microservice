package de.otto.edison.acceptance.health;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static de.otto.edison.acceptance.api.HealthApi.an_healthy_application;
import static de.otto.edison.acceptance.api.HealthApi.an_unhealthy_application;
import static de.otto.edison.acceptance.api.HealthApi.the_internal_health_is_retrieved;
import static de.otto.edison.acceptance.api.HealthApi.the_status_code;
import static de.otto.edison.testsupport.dsl.Given.given;
import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static de.otto.edison.testsupport.dsl.Then.then;
import static de.otto.edison.testsupport.dsl.When.when;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

public class HealthEndpointAcceptanceTest {

    @Test
    @Ignore("Disabled, because Spring Boot is caching health checks for one second.")
    public void shouldGetApplicationHealth() throws IOException {
        given(
                an_healthy_application()
        );
        when(
                the_internal_health_is_retrieved()
        );

        then(
                assertThat(
                        the_status_code(), is(OK)
                )
        );
    }

    @Test
    public void shouldBeUnhealty() throws IOException {
        given(
                an_unhealthy_application()

        );
        when(
                the_internal_health_is_retrieved()
        );

        then(
                assertThat(
                        the_status_code(), is(SERVICE_UNAVAILABLE)
                )
        );
    }


}
