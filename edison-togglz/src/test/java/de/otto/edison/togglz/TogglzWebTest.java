package de.otto.edison.togglz;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {TestServer.class})
@ActiveProfiles("test")
public class TogglzWebTest {

    private final static RestTemplate restTemplate = new RestTemplate();

    @LocalServerPort
    private int port;

    @Test
    public void shouldRegisterTogglzConsole() {
        final ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/togglztest/internal/toggles/", String.class);
        assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
    }

    @Test
    public void shouldAllowToggleStateToBeRetrievedInRequests() {
        final ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/togglztest/featurestate/test", String.class);
        assertThat(response.getStatusCode().is2xxSuccessful(), is(true));
        assertThat(response.getBody(), is("feature is active"));
    }
}