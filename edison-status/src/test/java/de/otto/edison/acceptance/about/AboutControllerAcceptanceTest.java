package de.otto.edison.acceptance.about;

import org.testng.annotations.Test;

import java.io.IOException;

import static de.otto.edison.acceptance.api.StatusApi.internal_about_is_retrieved_as;
import static de.otto.edison.acceptance.api.StatusApi.the_returned_content;
import static de.otto.edison.acceptance.api.StatusApi.the_returned_json;
import static de.otto.edison.acceptance.api.StatusApi.the_status_code;
import static de.otto.edison.testsupport.dsl.Then.and;
import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static de.otto.edison.testsupport.dsl.Then.then;
import static de.otto.edison.testsupport.dsl.When.when;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

public class AboutControllerAcceptanceTest {

    @Test
    public void shouldGetAboutAsHtml() throws IOException {
        when(
                internal_about_is_retrieved_as("text/html")
        );

        then(
                assertThat(the_status_code().value(), is(200)),
                and(
                        assertThat(the_returned_content(), startsWith("<!DOCTYPE html"))
                )
        );
    }

    @Test
    public void shouldGetInternalAboutAsJson() throws IOException {
        when(
                internal_about_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_status_code().value(), is(200))
        );
    }

    @Test
    public void shouldGetTeamInformation() throws IOException {
        when(
                internal_about_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_returned_json().at("/team/name").asText(), is("Test Team")),
                and(
                        assertThat(the_returned_json().at("/team/technicalContact").asText(), is("technical@example.org"))
                ),
                and(
                        assertThat(the_returned_json().at("/team/businessContact").asText(), is("business@example.org"))
                )
        );
    }

    @Test
    public void shouldGetVersionInformation() throws IOException {
        when(
                internal_about_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_returned_json().at("/vcs/version").asText(), is("1.0.0")),
                and(
                        assertThat(the_returned_json().at("/vcs/commit").asText(), is("ab1234"))
                ),
                and(
                        assertThat(the_returned_json().at("/vcs/url").asText(), is("http://example.org/vcs/1.0.0"))
                )
        );
    }

    @Test
    public void shouldGetApplicationInformation() throws IOException {
        when(
                internal_about_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_returned_json().at("/application/name").asText(), is("test")),
                assertThat(the_returned_json().at("/application/description").asText(), is("desc")),
                assertThat(the_returned_json().at("/application/environment").asText(), is("test-env")),
                assertThat(the_returned_json().at("/application/group").asText(), is("test-group"))
        );
    }

    @Test
    public void shouldGetServiceSpecs() throws IOException {
        when(
                internal_about_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_returned_json().at("/serviceSpecs/0/type").asText(), is("OTHER")),
                and(
                        assertThat(the_returned_json().at("/serviceSpecs/0/url").asText(), not(isEmptyString()))
                )
        );
    }

}
