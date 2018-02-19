package de.otto.edison.validation.validators;

import cz.jirutka.validator.collection.CommonEachValidator;
import cz.jirutka.validator.collection.constraints.EachConstraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@EachConstraint(validateAs = SafeId.class)
@Constraint(validatedBy = {CommonEachValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EachSafeId {

    String message() default "{invalid.id.value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
