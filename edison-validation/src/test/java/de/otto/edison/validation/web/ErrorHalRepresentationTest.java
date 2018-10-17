package de.otto.edison.validation.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ErrorHalRepresentationTest {

    @Test
    public void shouldSerializeAndDeserializeWithObjectMapper() throws IOException {
        // given
        final ErrorHalRepresentation errorHalRepresentation = ErrorHalRepresentation.builder()
                .withErrorMessage("some error message")
                .withError("field", "key", "message", "rejected")
                .build();
        final ObjectMapper objectMapper = new ObjectMapper();

        // when
        final String json = objectMapper.writeValueAsString(errorHalRepresentation);
        final ErrorHalRepresentation deserialized = objectMapper.readValue(json, ErrorHalRepresentation.class);

        // then
        assertThat(deserialized, is(errorHalRepresentation));
    }
}