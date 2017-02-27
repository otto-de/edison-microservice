package de.otto.edison.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ExampleMetricsSmokeTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;

    @Test
    public void shouldRenderMainPage() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/", String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).startsWith("<html");
    }

    @Test
    public void shouldRenderCacheStatisticsPage() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/internal/cacheinfos", String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).contains("\"name\" : \"Hello Cache\"");
    }

    @Test
    public void shouldHaveStatusEndpoint() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/internal/status.json", String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(response.getBody()).startsWith("{");
    }

    @Test
    public void shouldHaveHealthCheck() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/internal/health.json", String.class);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

}
