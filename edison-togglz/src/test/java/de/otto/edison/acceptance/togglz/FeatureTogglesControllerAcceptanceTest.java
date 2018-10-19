package de.otto.edison.acceptance.togglz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.togglz.TestServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static de.otto.edison.testsupport.dsl.Then.assertThat;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestServer.class})
@ActiveProfiles("test")
public class FeatureTogglesControllerAcceptanceTest {

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int port;

    @Test
    public void shouldTogglesAsJson() {
        // when
        ResponseEntity<String> resource = getResource("http://localhost:" + port + "/togglztest/internal/toggles");

        // then
        assertThat(jsonNode(resource).at("/features/TEST_FEATURE/description").asText(), is("a test feature toggle"));
        assertThat(jsonNode(resource).at("/features/TEST_FEATURE/enabled").asBoolean(), is(true));
        assertThat(jsonNode(resource).at("/features/TEST_FEATURE_2/description").asText(), is("TEST_FEATURE_2"));
        assertThat(jsonNode(resource).at("/features/TEST_FEATURE_2/enabled").asBoolean(), is(true));
    }

    ResponseEntity<String> getResource(final String url) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));

        return restTemplate.exchange(
                url,
                GET,
                new HttpEntity<>("parameters", headers), String.class
        );
    }

    JsonNode jsonNode(ResponseEntity<String> resource) {
        try {
            return objectMapper.readTree(resource.getBody());
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


}
