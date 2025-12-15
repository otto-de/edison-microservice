package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.TestServer;
import de.otto.edison.jobs.configuration.MockJobRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.client.ExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestServer.class, MockJobRunnable.class})
@ActiveProfiles("test")
public class JobDefinitionsControllerAcceptanceTest {

    @LocalServerPort
    private int port;

    private RestTestClient restTestClient;

    @BeforeEach
    public void setUp() {
        restTestClient = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    public void shouldRenderJobDefinitions() {

        // given / when / then
        restTestClient.get()
                .uri("/testjobs/internal/jobdefinitions")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body, containsString("<h3 class=\"panel-title\">" + MockJobRunnable.MOCK_JOB_NAME + "</h3>")));
    }

    @Test
    public void shouldRenderJobDefinitionByType() {

        // given / when / then
        restTestClient.get()
                .uri("/testjobs/internal/jobdefinitions/" + MockJobRunnable.MOCK_JOB_TYPE)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body, containsString("<h3 class=\"panel-title\">" + MockJobRunnable.MOCK_JOB_NAME + "</h3>")));
    }

}
