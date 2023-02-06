package de.otto.edison.validation.validators;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumListValidator implements ConstraintValidator<IsEnum, List<String>> {

    private static final String UNKNOWN_ENUMS_VALUE_MESSAGE_CODE = "unknown.enums.values";
    private final ResourceBundleMessageSource messageSource;
    private Set<String> availableEnumNames;
    private boolean ignoreCase;
    private boolean allowNull;

    public EnumListValidator(@Qualifier("edisonValidationMessageSource") ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void initialize(IsEnum annotation) {
        Class<? extends Enum<?>> enumClass = annotation.enumClass();
        availableEnumNames = Stream.of(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
        ignoreCase = annotation.ignoreCase();
        allowNull = annotation.allowNull();
    }

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        } else {
            List<String> invalidValues = value.stream()
                    .filter(v -> !isValidEnum(v))
                    .collect(Collectors.toList());

            if (!invalidValues.isEmpty()) {
                context.disableDefaultConstraintViolation();
                String invalidValuesString = String.join(",", invalidValues);
                String messageTemplate = messageSource.getMessage(UNKNOWN_ENUMS_VALUE_MESSAGE_CODE, new Object[]{invalidValuesString}, Locale.getDefault());
                context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
            }

            return invalidValues.isEmpty();
        }
    }

    private boolean isValidEnum(String enumString) {
        return availableEnumNames.stream().anyMatch(o -> {
            if (ignoreCase) {
                return o.equalsIgnoreCase(enumString);
            } else {
                return o.equals(enumString);
            }
        });
    }
}