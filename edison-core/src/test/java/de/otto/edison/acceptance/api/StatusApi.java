package de.otto.edison.acceptance.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.testsupport.applicationdriver.SpringTestBase;
import de.otto.edison.testsupport.dsl.When;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.parseMediaType;

public class StatusApi extends SpringTestBase {

    private final static RestTemplate restTemplate = new RestTemplate();
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private static String content = null;
    private static HttpStatusCode statusCode;
    private static HttpHeaders requestHeaders = new HttpHeaders();
    private static HttpHeaders responseHeaders;

    public static When internal_is_retrieved_as(final String mediaType) throws IOException {
        getResource("http://localhost:18084/testcore/internal", of(mediaType));
        return When.INSTANCE;
    }

    public static When internal_status_is_retrieved_as(final String mediaType) throws IOException {
        getResource("http://localhost:18084/testcore/internal/status", of(mediaType));
        return When.INSTANCE;
    }

    public static When internal_status_is_retrieved_as(final String mediaType, final HttpHeaders headers) throws IOException {
        requestHeaders = headers;
        getResource("http://localhost:18084/testcore/internal/status", of(mediaType));
        return When.INSTANCE;
    }

    public static HttpStatusCode the_status_code() {
        return statusCode;
    }

    public static String the_returned_content() {
        return content;
    }

    public static HttpHeaders the_response_headers() {
        return responseHeaders;
    }

    public static JsonNode the_returned_json() {
        try {
            return objectMapper.readTree(content);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static void getResource(final String url, final Optional<String> mediaType) throws IOException {
        if (mediaType.isPresent()) {
            requestHeaders.setAccept(asList(parseMediaType(mediaType.get())));
        }

        final ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                GET,
                new HttpEntity<>("parameters", requestHeaders), String.class
        );
        requestHeaders = new HttpHeaders();
        content = responseEntity.getBody();
        statusCode = responseEntity.getStatusCode();
        responseHeaders = responseEntity.getHeaders();
    }

}
