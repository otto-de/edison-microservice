package de.otto.edison.validation.web;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to mark classes that are used to hold request parameters. Used to determine which error code should be returned in case of validation errors.
 * If a request parameters class is annotated with `UrlParameterEntity` and a validation error occurs, a `400 BadRequest` error is thrown.
 * Otherwise, if a validation error occurs in a class without this annotation (e.g. for a request body) a `422 Unprocessable Entity` error is thrown.
 */
@Retention(value = RUNTIME)
@Target(value = {TYPE})
@Documented
public @interface UrlParameterEntity {

}
