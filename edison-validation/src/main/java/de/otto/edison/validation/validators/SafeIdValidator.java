package de.otto.edison.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class SafeIdValidator implements ConstraintValidator<SafeId, String> {

    private static final Pattern IdPattern = Pattern.compile("[a-zA-Z0-9\\-_]*");

    @Override
    public void initialize(SafeId safeId) {
        // do nothing
    }

    @Override
    public boolean isValid(String id, ConstraintValidatorContext context) {
        if (id == null) {
            return true;
        }
        return IdPattern.matcher(id).matches();
    }
}
