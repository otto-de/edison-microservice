package de.otto.edison.status.controller;

import de.otto.edison.logging.ui.LoggingTestServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = LoggingTestServer.class, webEnvironment = RANDOM_PORT,
        properties = "edison.application.favicon=/my-favicon.ico")
@AutoConfigureTestRestTemplate
public class FaviconAcceptanceTest {

    @Autowired
    private TestRestTemplate template;

    @Test
    public void shouldRenderFaviconLinkWhenConfigured() {
        final ResponseEntity<String> response = template.getForEntity("/internal/status?format=html", String.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), containsString("<link rel=\"icon\" href=\"/my-favicon.ico\""));
    }
}
