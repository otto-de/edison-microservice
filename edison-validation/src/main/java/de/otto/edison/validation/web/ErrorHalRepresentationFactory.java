package de.otto.edison.validation.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Locale;

import static java.util.Comparator.comparing;

@Component
public class ErrorHalRepresentationFactory {

    private final AbstractMessageSource messageSource;
    private final ObjectMapper objectMapper;
    private final String errorProfile;

    @Autowired
    public ErrorHalRepresentationFactory(
            AbstractMessageSource edisonValidationMessageSource,
            ObjectMapper objectMapper,
            @Value("${edison.validation.error-profile:http://spec.otto.de/profiles/error}") String errorProfile) {
        this.messageSource = edisonValidationMessageSource;
        this.objectMapper = objectMapper;
        this.errorProfile = errorProfile;
    }

    public ErrorHalRepresentation halRepresentationForErrorMessage(String errorMessage) {
        return ErrorHalRepresentation.builder()
                .withProfile(errorProfile)
                .withErrorMessage(errorMessage)
                .build();
    }

    public ErrorHalRepresentation halRepresentationForValidationErrors(Errors validationResult) {
        ErrorHalRepresentation.Builder builder = ErrorHalRepresentation.builder()
                .withProfile(errorProfile)
                .withErrorMessage(String.format("Validation failed. %d error(s)", validationResult.getErrorCount()));

        validationResult.getAllErrors()
                .stream()
                .filter(o -> o instanceof FieldError)
                .map(FieldError.class::cast)
                .sorted(comparing(FieldError::getField))
                .forEach(e -> builder.withError(e.getField(),
                        messageSource.getMessage(e.getCode() + ".key", null, "unknown", Locale.getDefault()),
                        e.getDefaultMessage(),
                        serializeRejectedValue(e)));

        return builder.build();
    }

    private String serializeRejectedValue(FieldError e) {
        if (e.getRejectedValue() == null) {
            return "null";
        }

        if (e.getRejectedValue() instanceof String) {
            return (String) e.getRejectedValue();
        } else {
            try {
                return objectMapper.writeValueAsString(e.getRejectedValue());
            } catch (JsonProcessingException ignore) {
                return e.getRejectedValue().toString();
            }
        }

    }


}
