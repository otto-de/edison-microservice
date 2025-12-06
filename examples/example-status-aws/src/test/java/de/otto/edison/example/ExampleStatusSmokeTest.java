package de.otto.edison.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.resttestclient.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ExampleStatusServer.class, webEnvironment = RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
public class ExampleStatusSmokeTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void shouldRenderMainPage() {
        final ResponseEntity<String> response = this.testRestTemplate.getForEntity("/", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).startsWith("<html");
    }

    @Test
    public void shouldHaveStatusEndpoint() {
        final ResponseEntity<String> response = this.testRestTemplate.getForEntity("/internal/status", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).startsWith("{");
    }

    @Test
    public void shouldHaveHealthCheck() {
        final ResponseEntity<String> response = this.testRestTemplate.getForEntity("/actuator/health", String.class);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getStatusCode().value()).isIn(200, 503);
    }

}
