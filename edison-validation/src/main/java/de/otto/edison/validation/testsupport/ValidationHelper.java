package de.otto.edison.validation.testsupport;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class ValidationHelper {
    private static Validator validator;

    static {
        PlatformResourceBundleLocator resourceBundleLocator = new PlatformResourceBundleLocator(ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES, null, true);
        ResourceBundleMessageInterpolator messageInterpolator = new ResourceBundleMessageInterpolator(resourceBundleLocator);
        validator = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(messageInterpolator)
                .buildValidatorFactory()
                .getValidator();
    }

    public static <T> List<String> getViolatedFields(T apiRepresentation) {
        Set<ConstraintViolation<T>> violations = validator.validate(apiRepresentation);
        return violations.stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Path::toString)
                .collect(toList());
    }
}
