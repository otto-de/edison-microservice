package de.otto.edison.validation.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Locale;

import static java.util.Comparator.comparing;

@Component
public class ErrorHalRepresentationFactory {

    private final ResourceBundleMessageSource messageSource;

    @Autowired
    public ErrorHalRepresentationFactory(ResourceBundleMessageSource edisonValidationMessageSource) {
        this.messageSource = edisonValidationMessageSource;
    }

    public ErrorHalRepresentation halRepresentationForValidationErrors(Errors validationResult) {
        ErrorHalRepresentation.Builder builder = ErrorHalRepresentation.builder()
                .withErrorMessage(String.format("Validation failed. %d error(s).", validationResult.getErrorCount()));

        validationResult.getAllErrors()
                .stream()
                .filter(o -> o instanceof FieldError)
                .map(FieldError.class::cast)
                .sorted(comparing(FieldError::getField))
                .forEach(e -> {
                    builder.withError(e.getField(),
                            messageSource.getMessage(e.getCode() + ".key", null, "unknown", Locale.getDefault()),
                            e.getDefaultMessage(),
                            e.getRejectedValue().toString());
                });

        return builder.build();
    }

}
