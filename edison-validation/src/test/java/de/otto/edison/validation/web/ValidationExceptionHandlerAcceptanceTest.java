package de.otto.edison.validation.web;

import de.otto.edison.validation.validators.SafeId;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.ServletContext;

import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableAutoConfiguration
@ComponentScan("de.otto.edison.validation")
@ContextConfiguration(classes = {
        ValidationExceptionHandler.class,
        ValidationExceptionHandlerAcceptanceTest.TestConfiguration.class})
public class ValidationExceptionHandlerAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ServletContext servletContext;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = servletContext.getContextPath();
    }

    @Test
    public void shouldValidateAndProduceErrorRepresentation() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"id\":\"_!NON_SAFE_ID!!?**\"}")
                .when()
                .put("/testing")
                .then()
                .assertThat()
                .statusCode(is(422)).and()
                .header("Content-type", Matchers.containsString("application/hal+json"))
                .body("errors.id[0].key", Collections.emptyList(), is("id.invalid"))
                .body("errors.id[0].message", Collections.emptyList(), is("Ungueltiger Id-Wert."))
                .body("errors.id[0].rejected", Collections.emptyList(), is("_!NON_SAFE_ID!!?**"));
    }

    @Test
    public void shouldValidateUrlParameterClassAndProduceBadRequestErrorRepresentation() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/testing?test=_!NON_SAFE_ID!!?**")
                .then()
                .assertThat()
                .statusCode(is(HttpStatus.BAD_REQUEST.value())).and()
                .header("Content-type", Matchers.containsString("application/hal+json"))
                .body("errors.test[0].key", Collections.emptyList(), is("id.invalid"))
                .body("errors.test[0].message", Collections.emptyList(), is("Ungueltiger Id-Wert."))
                .body("errors.test[0].rejected", Collections.emptyList(), is("_!NON_SAFE_ID!!?**"));
    }

    @Test
    public void shouldValidateSimpleUrlParameterAndProduceBadRequestErrorRepresentation() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/testing?simpleParam=_!NON_SAFE_ID!!?**")
                .then()
                .assertThat()
                .statusCode(is(HttpStatus.BAD_REQUEST.value())).and()
                .header("Content-type", Matchers.containsString("application/json"));
    }

    public static class TestConfiguration {
        @RestController
        public static class TestController {
            @RequestMapping(value = "/testing",
                    method = RequestMethod.PUT,
                    consumes = APPLICATION_JSON_VALUE,
                    produces = APPLICATION_JSON_VALUE)
            public String doTest(@Validated @RequestBody final ContentRepresentation content) {
                return "bla";
            }

            @RequestMapping(value = "/testing",
                    method = RequestMethod.GET,
                    produces = APPLICATION_JSON_VALUE)
            public String doTest(@RequestParam(value = "simpleParam", required = false) @SafeId String simpleParam, @Validated final TestUrlParameterEntity testUrlParameterEntity) {
                return "bla";
            }
        }
    }

    public static class ContentRepresentation {
        @SafeId
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @UrlParameterEntity
    public static class TestUrlParameterEntity {
        @SafeId
        private String test;

        public @SafeId String getTest() {
            return test;
        }

        public void setTest(@SafeId String test) {
            this.test = test;
        }
    }
}

