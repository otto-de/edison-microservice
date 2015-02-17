package de.otto.µservice.acceptance;

import org.testng.annotations.Test;

import java.io.IOException;

import static de.otto.µservice.acceptance.api.HealthApi.the_internal_health_is_retrieved;
import static de.otto.µservice.acceptance.api.HealthApi.the_status_code;
import static de.otto.µservice.testsupport.dsl.Then.assertThat;
import static de.otto.µservice.testsupport.dsl.Then.then;
import static de.otto.µservice.testsupport.dsl.When.when;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.OK;

public class HealthEndpointAcceptanceTest {

    @Test
    public void shouldGetApplicationHealth() throws IOException {
        when(
                the_internal_health_is_retrieved()
        );

        then(
                assertThat(
                        the_status_code(), is(OK)
                )
        );
    }

}
