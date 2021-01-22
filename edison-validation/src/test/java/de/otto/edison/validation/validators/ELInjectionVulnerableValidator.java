package de.otto.edison.validation.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ELInjectionVulnerableValidator implements ConstraintValidator<TestELInjectionValidation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("error message where input is reflected in output: " + value)
                .addConstraintViolation();

        return false;
    }
}
