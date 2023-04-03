package de.otto.edison.validation.validators;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumValidator implements ConstraintValidator<IsEnum, String> {

    private Set<String> availableEnumNames;
    private boolean ignoreCase;
    private boolean allowNull;

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
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        } else {
            return availableEnumNames.stream().anyMatch(o -> {
                if (ignoreCase) {
                    return o.equalsIgnoreCase(value);
                } else {
                    return o.equals(value);
                }
            });
        }
    }

}
