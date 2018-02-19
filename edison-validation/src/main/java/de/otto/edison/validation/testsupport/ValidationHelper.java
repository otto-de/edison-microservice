package de.otto.edison.validation.testsupport;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class ValidationHelper {
    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> List<String> getViolatedFields(T apiRepresentation) {
        Set<ConstraintViolation<T>> violations = validator.validate(apiRepresentation);
        return violations.stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Path::toString)
                .collect(toList());
    }
}
