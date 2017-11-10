package de.otto.edison.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleJobsServer.class, webEnvironment = RANDOM_PORT)
public class ExampleJobsSmokeTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;

    @Test
    public void shouldRenderMainPage() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/", String.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).startsWith("<html");
        assertThat(response.getBody()).contains("<p >Hello Microservice Edison</p>");
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
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/application/health", String.class);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void shouldHaveJobDefinitions() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/internal/jobdefinitions.json", String.class);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("{\n" +
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
                "    \"href\" : \"http://localhost:" + port + "/internal/jobdefinitions\",\n" +
                "    \"rel\" : \"self\",\n" +
                "    \"title\" : \"Self\"\n" +
                "  } ]\n" +
                "}");
    }

    @Test
    public void shouldHaveFooJobDefinition() {
        final ResponseEntity<String> response = this.restTemplate.getForEntity("/internal/jobdefinitions/foo.json", String.class);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON_UTF8);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("{\n" +
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
                "}");
    }

    @Test
    public void shouldTriggerJob() {
        final ResponseEntity<String> postResponse = restTemplate.postForEntity("/internal/jobs/Foo", "", String.class);
        assertThat(postResponse.getStatusCodeValue()).isEqualTo(204);
        final ResponseEntity<String> jobResponse = restTemplate.getForEntity(postResponse.getHeaders().getLocation(), String.class);
        assertThat(jobResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(jobResponse.getBody()).contains("\"state\" : \"Running\"");
        assertThat(jobResponse.getBody()).contains("\"jobType\" : \"Foo\"");
    }

}
