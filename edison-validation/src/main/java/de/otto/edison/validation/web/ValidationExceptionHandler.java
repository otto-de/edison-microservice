package de.otto.edison.validation.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
public class ValidationExceptionHandler {

    private static final MediaType APPLICATION_HAL_JSON_ERROR = MediaType.parseMediaType("application/hal+json; " +
            "profiles=\"http://spec.otto.de/profiles/error\"");
    private final ErrorHalRepresentationFactory errorHalRepresentationFactory;

    @Autowired
    public ValidationExceptionHandler(ErrorHalRepresentationFactory errorHalRepresentationFactory) {
        this.errorHalRepresentationFactory = errorHalRepresentationFactory;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = UNPROCESSABLE_ENTITY)
    public ResponseEntity<ErrorHalRepresentation> handleException(final MethodArgumentNotValidException exception) {
        HttpStatus returnCode = UNPROCESSABLE_ENTITY;
        if (exception.getTarget().getClass().isAnnotationPresent(UrlParameterEntity.class)) {
            returnCode = BAD_REQUEST;
        }

        return ResponseEntity.status(returnCode)
                .contentType(APPLICATION_HAL_JSON_ERROR)
                .body(errorHalRepresentationFactory.halRepresentationForValidationErrors(exception.getBindingResult()));
    }
}