package de.otto.edison.validation.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class InstantValidator implements ConstraintValidator<IsInstant, String> {

    @Override
    public void initialize(IsInstant constraintAnnotation) {
        // do nothing
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            Instant.parse(value);
        } catch (DateTimeParseException e){
            return false;
        }
        return true;
    }
}
