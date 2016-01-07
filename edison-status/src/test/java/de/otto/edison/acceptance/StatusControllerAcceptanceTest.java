package de.otto.edison.acceptance;

import org.testng.annotations.Test;

import java.io.IOException;

import static de.otto.edison.acceptance.api.StatusApi.the_internal_status_is_retrieved_as;
import static de.otto.edison.acceptance.api.StatusApi.the_returned_content;
import static de.otto.edison.acceptance.api.StatusApi.the_returned_json;
import static de.otto.edison.acceptance.api.StatusApi.the_status_code;
import static de.otto.edison.testsupport.dsl.Then.and;
import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static de.otto.edison.testsupport.dsl.Then.then;
import static de.otto.edison.testsupport.dsl.When.when;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

public class StatusControllerAcceptanceTest {

    @Test
    public void shouldGetApplicationStatus() throws IOException {
        when(
                the_internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                and(
                        assertThat(the_returned_json().at("/application/status").asText(), is("WARNING"))),
                and(
                        assertThat(the_returned_json().at("/application/name").asText(), is("teststatus"))
                )
        );
    }

    @Test
    public void shouldGetApplicationStatusDetailsAsJson() throws IOException {
        when(
                the_internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                and(
                        assertThat(the_returned_json().at("/application/statusDetails/foo/status").asText(), is("OK"))
                ),
                and(
                        assertThat(the_returned_json().at("/application/statusDetails/bar/status").asText(), is("WARNING"))
                )
        );
    }

    @Test
    public void shouldGetApplicationStatusWithVcsInformation() throws IOException {
        when(
                the_internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                and(
                        assertThat(the_returned_json().at("/application/version").asText(), is("unknown"))
                )
        );
    }

    @Test
    public void shouldGetApplicationStatusAsHtml() throws IOException {
        when(
                the_internal_status_is_retrieved_as("text/html")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                and(
                    assertThat(the_returned_content(), startsWith("<!DOCTYPE html>"))
                )
        );
    }
}
