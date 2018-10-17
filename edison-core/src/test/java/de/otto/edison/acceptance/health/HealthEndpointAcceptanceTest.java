package de.otto.edison.acceptance.health;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static de.otto.edison.acceptance.api.HealthApi.an_unhealthy_application;
import static de.otto.edison.acceptance.api.HealthApi.the_internal_health_is_retrieved;
import static de.otto.edison.acceptance.api.HealthApi.the_status_code;
import static de.otto.edison.testsupport.dsl.Given.given;
import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static de.otto.edison.testsupport.dsl.Then.then;
import static de.otto.edison.testsupport.dsl.When.when;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

public class HealthEndpointAcceptanceTest {

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
