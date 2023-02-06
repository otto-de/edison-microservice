package de.otto.edison.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ExampleJobsServer.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class ExampleJobsSmokeTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldRenderMainPage() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/", String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).startsWith("<html");
        assertThat(response.getBody()).contains("<p >Hello Microservice Edison</p>");
    }

    @Test
    public void shouldHaveStatusEndpoint() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/internal/status?format=json", String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).startsWith("{");
    }

    @Test
    public void shouldHaveHealthCheck() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void shouldHaveJobDefinitions() throws JSONException {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/internal/jobdefinitions?format=json", String.class);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        JSONAssert.assertEquals("{\n" +
                "  \"links\" : [ {\n" +
                "    \"href\" : \"http://localhost:" + port + "/internal/jobdefinitions/Bar\",\n" +
                "    \"rel\" : \"http://github.com/otto-de/edison/link-relations/job/definition\",\n" +
                "    \"title\" : \"Bar Job\"\n" +
                "  }, {\n" +
                "    \"href\" : \"http://localhost:" + port + "/internal/jobdefinitions/ExampleMetaJob\",\n" +
                "    \"rel\" : \"http://github.com/otto-de/edison/link-relations/job/definition\",\n" +
                "    \"title\" : \"Some stateful Job\"\n" +
                "  }, {\n" +
                "    \"href\" : \"http://localhost:" + port + "/internal/jobdefinitions/Fizzle\",\n" +
                "    \"rel\" : \"http://github.com/otto-de/edison/link-relations/job/definition\",\n" +
                "    \"title\" : \"Fizzle Job\"\n" +
                "  }, {\n" +
                "    \"href\" : \"http://localhost:" + port + "/internal/jobdefinitions/Foo\",\n" +
                "    \"rel\" : \"http://github.com/otto-de/edison/link-relations/job/definition\",\n" +
                "    \"title\" : \"Foo Job\"\n" +
                "  }, {\n" +
                "    \"href\" : \"http://localhost:" + port + "/internal/jobdefinitions/Old\",\n" +
                "    \"rel\" : \"http://github.com/otto-de/edison/link-relations/job/definition\",\n" +
                "    \"title\" : \"Old Job\"\n" +
                "  }, {\n" +
                "    \"href\" : \"http://localhost:" + port + "/internal/jobdefinitions\",\n" +
                "    \"rel\" : \"self\",\n" +
                "    \"title\" : \"Self\"\n" +
                "  } ]\n" +
                "}", response.getBody(), true);
    }

    @Test
    public void shouldHaveFooJobDefinition() throws JSONException {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/internal/jobdefinitions/foo?format=json", String.class);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        JSONAssert.assertEquals("{\n" +
                "  \"type\" : \"Foo\",\n" +
                "  \"name\" : \"Foo Job\",\n" +
                "  \"retries\" : 0,\n" +
                "  \"maxAge\" : 10800,\n" +
                "  \"fixedDelay\" : 3600,\n" +
                "  \"links\" : [ {\n" +
                "    \"href\" : \"http://localhost:" + port + "/internal/jobsdefinitions/Foo\",\n" +
                "    \"rel\" : \"self\"\n" +
                "  }, {\n" +
                "    \"href\" : \"http://localhost:" + port + "/internal/jobdefinitions\",\n" +
                "    \"rel\" : \"collection\"\n" +
                "  }, {\n" +
                "    \"href\" : \"http://localhost:" + port + "/internal/jobs/Foo\",\n" +
                "    \"rel\" : \"http://github.com/otto-de/edison/link-relations/job/trigger\"\n" +
                "  } ]\n" +
                "}", response.getBody(), true);
    }

    @Test
    public void shouldTriggerJob() {
        final ResponseEntity<String> postResponse = restTemplate.postForEntity("/internal/jobs/Foo", "", String.class);
        assertThat(postResponse.getStatusCodeValue()).isEqualTo(204);
        final ResponseEntity<String> jobResponse = restTemplate.getForEntity(postResponse.getHeaders().getLocation(), String.class);
        assertThat(jobResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(jobResponse.getBody()).containsPattern(("\"state\"( )*:( )*\"Running\"")); // contains ignoring whitespaces
        assertThat(jobResponse.getBody()).containsPattern("\"jobType\"( )*:( )*\"Foo\""); // contains ignoring whitespaces
    }

    @Test
    public void shouldUseJobEventPublisherForOldJob() throws IOException, InterruptedException {
        final ResponseEntity<String> postResponse = restTemplate.postForEntity("/internal/jobs/Old", "", String.class);
        assertThat(postResponse.getStatusCodeValue()).isEqualTo(204);
        ResponseEntity<String> jobResponse = restTemplate.getForEntity(postResponse.getHeaders().getLocation(), String.class);
        assertThat(jobResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(jobResponse.getBody()).containsPattern(("\"state\"( )*:( )*\"Running\"")); // contains ignoring whitespaces
        assertThat(jobResponse.getBody()).containsPattern("\"jobType\"( )*:( )*\"Old\""); // contains ignoring whitespaces

        Thread.sleep(2000);
        jobResponse = restTemplate.getForEntity(postResponse.getHeaders().getLocation(), String.class);
        assertThat(jobResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(jobResponse.getBody()).containsPattern(("\"state\"( )*:( )*\"Stopped\"")); // contains ignoring whitespaces
        assertThat(jobResponse.getBody()).containsPattern(("\"status\"( )*:( )*\"SKIPPED\"")); // contains ignoring whitespaces
        assertThat(jobResponse.getBody()).containsPattern("( )*Still doing some work...( )*"); // contains ignoring whitespaces

    }

}
