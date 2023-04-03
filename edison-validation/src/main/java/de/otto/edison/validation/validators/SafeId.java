package de.otto.edison.validation.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Documented
@Constraint(validatedBy = {SafeIdValidator.class})
@Target({METHOD, FIELD, PARAMETER, TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeId {

    String message() default "{invalid.id.value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
