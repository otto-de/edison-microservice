package de.otto.edison.validation.validators;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumSetValidator implements ConstraintValidator<IsEnum, Set<String>> {

    private static final String UNKNOWN_ENUMSET_VALUE_MESSAGE_CODE = "unknown.enumset.values";
    private final ResourceBundleMessageSource messageSource;
    private Set<String> availableEnumNames;
    private boolean ignoreCase;
    private boolean allowNull;

    public EnumSetValidator(@Qualifier("edisonValidationMessageSource") ResourceBundleMessageSource messageSource) {
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
    public boolean isValid(Set<String> value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        } else {
            Set<String> invalidValues = value.stream()
                    .filter(v -> !isValidEnum(v))
                    .collect(Collectors.toSet());

            if (!invalidValues.isEmpty()) {
                context.disableDefaultConstraintViolation();
                String invalidValuesString = String.join(",", invalidValues);
                String messageTemplate = messageSource.getMessage(UNKNOWN_ENUMSET_VALUE_MESSAGE_CODE, new Object[] {invalidValuesString}, Locale.getDefault());
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