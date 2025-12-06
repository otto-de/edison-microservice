package de.otto.edison.validation.web;

import de.otto.edison.validation.validators.SafeId;
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
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.*;

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
    private WebApplicationContext webApplicationContext;

    private RestTestClient restTestClient;

    @BeforeEach
    public void setUp() {
        //restTestClient = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        restTestClient = RestTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Test
    public void shouldValidateAndProduceErrorRepresentation() {
        restTestClient.put()
                .uri("/testing")
                .contentType(APPLICATION_JSON)
                .body("{\"id\":\"_!NON_SAFE_ID!!?**\"}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectHeader().value("Content-Type", it -> assertThat(it, containsString("application/hal+json")))
                .expectBody()
                .jsonPath("$.errors.id[0].key").isEqualTo("id.invalid")
                .jsonPath("$.errors.id[0].message").isEqualTo("Ungueltiger Id-Wert.")
                .jsonPath("$.errors.id[0].rejected").isEqualTo("_!NON_SAFE_ID!!?**");
    }

    @Test
    public void shouldValidateUrlParameterClassAndProduceBadRequestErrorRepresentation() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/testing")
                        .queryParam("test", "_!NON_SAFE_ID!!?**")
                        .build())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectHeader().value("Content-Type", it -> assertThat(it, containsString("application/hal+json")))
                .expectBody()
                .jsonPath("$.errors.test[0].key").isEqualTo("id.invalid")
                .jsonPath("$.errors.test[0].message").isEqualTo("Ungueltiger Id-Wert.")
                .jsonPath("$.errors.test[0].rejected").isEqualTo("_!NON_SAFE_ID!!?**");
    }

    @Test
    public void shouldValidateSimpleUrlParameterAndProduceBadRequestErrorRepresentation() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/testing")
                        .queryParam("simpleParam", "_!NON_SAFE_ID!!?**")
                        .build())
                .header("Content-Type", "application/json")
                .accept(ALL)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
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

