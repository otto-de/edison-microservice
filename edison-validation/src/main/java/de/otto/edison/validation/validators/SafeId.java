package de.otto.edison.validation.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {SafeIdValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SafeId {

    String message() default "{invalid.id.value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
