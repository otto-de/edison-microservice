package de.otto.edison.acceptance.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

import static de.otto.edison.acceptance.api.StatusApi.internal_is_retrieved_as;
import static de.otto.edison.acceptance.api.StatusApi.internal_resource_is_retrieved_as;
import static de.otto.edison.acceptance.api.StatusApi.internal_status_is_retrieved_as;
import static de.otto.edison.acceptance.api.StatusApi.the_response_headers;
import static de.otto.edison.acceptance.api.StatusApi.the_returned_content;
import static de.otto.edison.acceptance.api.StatusApi.the_returned_json;
import static de.otto.edison.acceptance.api.StatusApi.the_status_code;
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
                assertThat(the_response_headers().get("Content-Type"), contains("application/vnd.otto.monitoring.status+json"))
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
                assertThat(the_returned_json().at("/application/name").asString(), is("test-app")),
                assertThat(the_returned_json().at("/application/description").asString(), is("desc")),
                assertThat(the_returned_json().at("/application/environment").asString(), is("test-env")),
                assertThat(the_returned_json().at("/application/group").asString(), is("test-group"))
        );

    }

    @Test
    public void shouldGetVersionInformation() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_returned_json().at("/application/version").asString(), is("1.0.0")),
                assertThat(the_returned_json().at("/application/commit").asString(), is("ab1234")),
                assertThat(the_returned_json().at("/application/vcsUrl").asString(), is("http://example.org/vcs/1.0.0"))
        );
    }

    @Test
    public void shouldGetTeamInformation() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_returned_json().at("/team/name").asString(), is("Test Team")),
                assertThat(the_returned_json().at("/team/technicalContact").asString(), is("technical@example.org")),
                assertThat(the_returned_json().at("/team/businessContact").asString(), is("business@example.org"))
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
                assertThat(the_returned_json().at("/cluster/color").asString(), is("BLU")),
                assertThat(the_returned_json().at("/cluster/colorState").asString(), is("STAGED"))
        );
    }

    @Test
    public void shouldGetStatusWithDetails() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                assertThat(the_returned_json().at("/application/status").asString(), is("WARNING")),
                assertThat(the_returned_json().at("/application/statusDetails/foo/status").asString(), is("OK")),
                assertThat(the_returned_json().at("/application/statusDetails/bar/status").asString(), is("WARNING"))
        );
    }

    @Test
    public void shouldGetStatusWithCriticality() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                assertThat(the_returned_json().at("/criticality/level").asString(), is("LOW")),
                assertThat(the_returned_json().at("/criticality/disasterImpact").asString(), is("some impact"))
        );
    }

    @Test
    public void shouldGetStatusWithDependencies() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                assertThat(the_returned_json().at("/dependencies/0/url").asString(), is("http://example.com/foo"))
        );
    }

    @Test
    public void shouldServeInternalJavascriptResources() throws IOException {
        when(
                internal_resource_is_retrieved_as("js/themeInit.js", "text/javascript")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                assertThat(the_returned_content(), containsString("localStorage.getItem('theme')"))
        );

        when(
                internal_resource_is_retrieved_as("js/darkmodeToggle.js", "text/javascript")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                assertThat(the_returned_content(), containsString("darkModeToggle"))
        );
    }
}
