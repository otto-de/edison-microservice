package de.otto.edison.acceptance.status;

import org.testng.annotations.Test;

import java.io.IOException;

import static de.otto.edison.acceptance.api.StatusApi.internal_status_is_retrieved_as;
import static de.otto.edison.acceptance.api.StatusApi.the_returned_content;
import static de.otto.edison.acceptance.api.StatusApi.the_returned_json;
import static de.otto.edison.acceptance.api.StatusApi.the_status_code;
import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static de.otto.edison.testsupport.dsl.Then.then;
import static de.otto.edison.testsupport.dsl.When.when;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

public class StatusControllerAcceptanceTest {

    @Test
    public void shouldGetInternalStatusAsHtml() throws IOException {
        when(
                internal_status_is_retrieved_as("text/html")
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
                assertThat(the_returned_json().at("/application/appId").asText(), is("/test-env/test-group/test-app")),
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
    public void shouldGetServiceSpecs() throws IOException {
        when(
                internal_status_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_returned_json().at("/serviceSpecs/0/appId").asText(), is("/test/bar")),
                assertThat(the_returned_json().at("/serviceSpecs/0/url").asText(), not(isEmptyString())),
                assertThat(the_returned_json().at("/serviceSpecs/0/type/type").asText(), is("TEST")),
                assertThat(the_returned_json().at("/serviceSpecs/0/type/criticality").asText(), is("MISSION_CRITICAL")),
                assertThat(the_returned_json().at("/serviceSpecs/0/type/disasterImpact").asText(), is("test will fail")),
                assertThat(the_returned_json().at("/serviceSpecs/0/expectations/availability").asText(), is("HIGH")),
                assertThat(the_returned_json().at("/serviceSpecs/0/expectations/performance").asText(), is("HIGH")),

                assertThat(the_returned_json().at("/serviceSpecs/1/appId").asText(), is("/test/foo")),
                assertThat(the_returned_json().at("/serviceSpecs/1/url").asText(), not(isEmptyString())),
                assertThat(the_returned_json().at("/serviceSpecs/1/type/type").asText(), is("not specified")),
                assertThat(the_returned_json().at("/serviceSpecs/1/type/criticality").asText(), is("NOT_SPECIFIED")),
                assertThat(the_returned_json().at("/serviceSpecs/1/type/disasterImpact").asText(), is("not specified")),
                assertThat(the_returned_json().at("/serviceSpecs/1/expectations/availability").asText(), is("NOT_SPECIFIED")),
                assertThat(the_returned_json().at("/serviceSpecs/1/expectations/performance").asText(), is("NOT_SPECIFIED"))
        );
    }

}
