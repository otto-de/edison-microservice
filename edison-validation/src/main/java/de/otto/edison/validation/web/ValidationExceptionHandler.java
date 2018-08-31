package de.otto.edison.validation.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.ResponseEntity.unprocessableEntity;

@ControllerAdvice
public class ValidationExceptionHandler {

    private static final MediaType APPLICATION_HAL_JSON_ERROR = MediaType.parseMediaType("application/hal+json; " +
            "profiles=\"http://spec.otto.de/profiles/error\"; charset=utf-8");
    private final ErrorHalRepresentationFactory errorHalRepresentationFactory;

    @Autowired
    public ValidationExceptionHandler(ErrorHalRepresentationFactory errorHalRepresentationFactory) {
        this.errorHalRepresentationFactory = errorHalRepresentationFactory;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = UNPROCESSABLE_ENTITY)
    public ResponseEntity<ErrorHalRepresentation> handleException(final MethodArgumentNotValidException exception) {
        return unprocessableEntity()
                .contentType(APPLICATION_HAL_JSON_ERROR)
                .body(errorHalRepresentationFactory.halRepresentationForValidationErrors(exception.getBindingResult()));
    }
}