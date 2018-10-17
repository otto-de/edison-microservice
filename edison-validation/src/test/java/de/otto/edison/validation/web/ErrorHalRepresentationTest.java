package de.otto.edison.validation.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ErrorHalRepresentationTest {

    @Test
    public void shouldSerializeAndDeserializeWithObjectMapper() throws IOException {
        // given
        ErrorHalRepresentation errorHalRepresentation = ErrorHalRepresentation.builder()
                .withErrorMessage("some error message")
                .withError("field", "key", "message", "rejected")
                .build();
        ObjectMapper objectMapper = new ObjectMapper();

        // when
        String json = objectMapper.writeValueAsString(errorHalRepresentation);
        ErrorHalRepresentation deserialized = objectMapper.readValue(json, ErrorHalRepresentation.class);

        // then
        assertThat(deserialized, is(errorHalRepresentation));
    }
}