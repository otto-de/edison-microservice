package de.otto.edison.acceptance.status;

import org.testng.annotations.Test;

import java.io.IOException;

import static de.otto.edison.acceptance.api.FeatureTogglesApi.internal_toggles_is_retrieved_as;
import static de.otto.edison.acceptance.api.FeatureTogglesApi.the_returned_json;
import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static de.otto.edison.testsupport.dsl.Then.then;
import static de.otto.edison.testsupport.dsl.When.when;
import static org.hamcrest.Matchers.is;

public class FeatureTogglesControllerAcceptanceTest {

    @Test
    public void shouldTogglesAsJson() throws IOException {
        when(
                internal_toggles_is_retrieved_as("application/json")
        );

        then(
                assertThat(the_returned_json().at("/features/TEST_FEATURE/description").asText(), is("a test feature toggle")),
                assertThat(the_returned_json().at("/features/TEST_FEATURE/enabled").asBoolean(), is(true)),
                assertThat(the_returned_json().at("/features/TEST_FEATURE_2/description").asText(), is("TEST_FEATURE_2")),
                assertThat(the_returned_json().at("/features/TEST_FEATURE_2/enabled").asBoolean(), is(true))
        );

    }

}
