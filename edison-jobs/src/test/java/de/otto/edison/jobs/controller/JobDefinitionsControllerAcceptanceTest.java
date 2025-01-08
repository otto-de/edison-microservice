package de.otto.edison.jobs.controller;

import de.otto.edison.jobs.TestServer;
import de.otto.edison.jobs.configuration.MockJobRunnable;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.Matchers.containsString;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestServer.class, MockJobRunnable.class})
@ActiveProfiles("test")
public class JobDefinitionsControllerAcceptanceTest {

    @LocalServerPort
    private int port;

    @Test
    public void shouldRenderJobDefinitions() {
        // given
        RestAssured.given()
                .port(port)

                // when
                .when()
                .get("/testjobs/internal/jobdefinitions")

                // then
                .then()
                .statusCode(200)
                .body(containsString("<h3 class=\"panel-title\">" + MockJobRunnable.MOCK_JOB_NAME + "</h3>"));
    }

    @Test
    public void shouldRenderJobDefinitionByType() {
        // given
        RestAssured.given()
                .port(port)

                // when
                .when()
                .get("/testjobs/internal/jobdefinitions/" + MockJobRunnable.MOCK_JOB_TYPE)

                // then
                .then()
                .statusCode(200)
                .body(containsString("<h3 class=\"panel-title\">" + MockJobRunnable.MOCK_JOB_NAME + "</h3>"));
    }

}
