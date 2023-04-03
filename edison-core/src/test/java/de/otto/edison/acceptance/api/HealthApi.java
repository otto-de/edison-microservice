package de.otto.edison.acceptance.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.health.indicator.ApplicationHealthIndicator;
import de.otto.edison.testsupport.applicationdriver.SpringTestBase;
import de.otto.edison.testsupport.dsl.Given;
import de.otto.edison.testsupport.dsl.When;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.up;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.parseMediaType;

public class HealthApi extends SpringTestBase {

    private final static RestTemplate restTemplate = new RestTemplate();
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private static String content = null;
    private static HttpStatusCode statusCode;

    public static Given an_healthy_application() {
        ApplicationHealthIndicator healthIndicator = applicationContext().getBean(ApplicationHealthIndicator.class);
        healthIndicator.indicateHealth(up().build());
        return Given.INSTANCE;
    }

    public static Given an_unhealthy_application() {
        ApplicationHealthIndicator healthIndicator = applicationContext().getBean(ApplicationHealthIndicator.class);
        healthIndicator.indicateHealth(down().build());
        return Given.INSTANCE;
    }

    public static When the_internal_health_is_retrieved() throws IOException {
        getResource("http://localhost:18084/testcore/actuator/health", Optional.<String>empty());
        return When.INSTANCE;
    }

    private static void getResource(final String url, final Optional<String> mediaType) {
        final HttpHeaders headers = new HttpHeaders();
        if (mediaType.isPresent()) {
            headers.setAccept(asList(parseMediaType(mediaType.get())));
        }
        try {
            final ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    GET,
                    new HttpEntity<>("parameters", headers), String.class
            );
            content = responseEntity.getBody();
            statusCode = responseEntity.getStatusCode();
        } catch (HttpStatusCodeException e) {
            content = e.getStatusText();
            statusCode = e.getStatusCode();
        }
    }

    public static HttpStatusCode the_status_code() {
        return statusCode;
    }

    public static String the_returned_content() {
        return content;
    }

    public static JsonNode the_returned_json() {
        try {
            return objectMapper.readTree(content);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
