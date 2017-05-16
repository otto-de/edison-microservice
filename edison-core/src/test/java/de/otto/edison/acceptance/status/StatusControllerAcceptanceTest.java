package de.otto.edison.acceptance.status;

import org.junit.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

import static de.otto.edison.acceptance.api.StatusApi.*;
import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static de.otto.edison.testsupport.dsl.Then.then;
import static de.otto.edison.testsupport.dsl.When.when;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

public class StatusControllerAcceptanceTest {

    @Test
    public void shouldGetInternalStatusAsHtml() throws IOException {

        when(
                internal_status_is_retrieved_as("text/html")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                assertThat(the_returned_content(), startsWith("<!DOCTYPE html>")),
                assertThat(the_returned_content(), containsString("<title>Some Test</title>"))
        );
    }

    @Test
    public void shouldGetInternalStatusAsMonitoringStatusJson() throws IOException {
        when(
                internal_status_is_retrieved_as("application/vnd.otto.monitoring.status+json")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                assertThat(the_response_headers().get("Content-Type"), contains("application/vnd.otto.monitoring.status+json;charset=UTF-8"))
        );
    }

    @Test
    public void shouldRedirectInternalToStatus() throws IOException {
        when(
                internal_is_retrieved_as("text/html")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                assertThat(the_returned_content(), startsWith("<!DOCTYPE html>"))
        );
    }

    @Test
    public void shouldGetApplicationInfo() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_returned_json().at("/application/name").asText(), is("test-app")),
                assertThat(the_returned_json().at("/application/description").asText(), is("desc")),
                assertThat(the_returned_json().at("/application/environment").asText(), is("test-env")),
                assertThat(the_returned_json().at("/application/group").asText(), is("test-group"))
        );

    }

    @Test
    public void shouldGetVersionInformation() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_returned_json().at("/application/version").asText(), is("1.0.0")),
                assertThat(the_returned_json().at("/application/commit").asText(), is("ab1234")),
                assertThat(the_returned_json().at("/application/vcsUrl").asText(), is("http://example.org/vcs/1.0.0"))
        );
    }

    @Test
    public void shouldGetTeamInformation() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_returned_json().at("/team/name").asText(), is("Test Team")),
                assertThat(the_returned_json().at("/team/technicalContact").asText(), is("technical@example.org")),
                assertThat(the_returned_json().at("/team/businessContact").asText(), is("business@example.org"))
        );
    }

    @Test
    public void shouldGetClusterInformation() throws IOException {
        final HttpHeaders headers = new HttpHeaders();
        headers.put("X-Color", singletonList("BLU"));
        headers.put("X-Staging", singletonList("STAGED"));
        when(
                internal_status_is_retrieved_as("application/json", headers)
        );

        then(
                assertThat(the_returned_json().at("/cluster/color").asText(), is("BLU")),
                assertThat(the_returned_json().at("/cluster/colorState").asText(), is("STAGED"))
        );
    }

    @Test
    public void shouldGetStatusWithDetails() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                assertThat(the_returned_json().at("/application/status").asText(), is("WARNING")),
                assertThat(the_returned_json().at("/application/statusDetails/foo/status").asText(), is("OK")),
                assertThat(the_returned_json().at("/application/statusDetails/bar/status").asText(), is("WARNING"))
        );
    }

    @Test
    public void shouldGetStatusWithCriticality() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                assertThat(the_returned_json().at("/criticality/level").asText(), is("LOW")),
                assertThat(the_returned_json().at("/criticality/disasterImpact").asText(), is("some impact"))
        );
    }

    @Test
    public void shouldGetStatusWithDependencies() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                assertThat(the_returned_json().at("/dependencies/0/url").asText(), is("http://example.com/foo"))
        );
    }

}
