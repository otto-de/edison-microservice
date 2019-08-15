package de.otto.edison.validation.validators;

import org.hibernate.validator.internal.engine.DefaultClockProvider;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.validation.ConstraintValidatorContext;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnumSetValidatorTest {

    enum TestEnum {
        foo,
        bar
    }

    @Test
    public void shouldValidateSuccessfully() throws Exception {
        EnumSetValidator enumSetValidator = createAndInitializeValidator(false, false);
        boolean valid = enumSetValidator.isValid(createSet("foo", "bar"), createContext());
        assertThat(valid, is(true));
    }

    @Test
    public void shouldFailForInvalidValue() throws Exception {
        EnumSetValidator enumSetValidator = createAndInitializeValidator(false, false);
        boolean valid = enumSetValidator.isValid(createSet("foo", "xxx"), createContext());
        assertThat(valid, is(false));
    }

    @Test
    public void shouldFailWhenNotIgnoringCase() throws Exception {
        EnumSetValidator enumSetValidator = createAndInitializeValidator(false, false);
        ConstraintValidatorContext context = createContext();
        boolean valid = enumSetValidator.isValid(createSet("Foo"), context);
        assertThat(valid, is(false));
    }

    @Test
    public void shouldSucceedWhenIgnoringCase() throws Exception {
        EnumSetValidator enumSetValidator = createAndInitializeValidator(true, false);
        boolean valid = enumSetValidator.isValid(createSet("Foo"), createContext());
        assertThat(valid, is(true));
    }

    @Test
    public void shouldFailForNull() {
        EnumSetValidator enumSetValidator = createAndInitializeValidator(true, false);
        boolean valid = enumSetValidator.isValid(null, createContext());
        assertThat(valid, is(false));
    }

    @Test
    public void shouldFailForNullValue() {
        EnumSetValidator enumSetValidator = createAndInitializeValidator(true, false);
        boolean valid = enumSetValidator.isValid(createSet((String) null), createContext());
        assertThat(valid, is(false));
    }

    @Test
    public void shouldAllowNullWhenFlagIsSet() {
        EnumSetValidator enumSetValidator = createAndInitializeValidator(true, true);
        boolean valid = enumSetValidator.isValid(null, createContext());
        assertThat(valid, is(true));
    }

    @Test
    public void shouldFailForEmptyString() {
        EnumSetValidator enumSetValidator = createAndInitializeValidator(true, false);
        boolean valid = enumSetValidator.isValid(createSet(""), createContext());
        assertThat(valid, is(false));
    }

    private EnumSetValidator createAndInitializeValidator(boolean ignoreCase, boolean allowNull) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("ValidationMessages");
        messageSource.setUseCodeAsDefaultMessage(true);

        EnumSetValidator enumSetValidator = new EnumSetValidator(messageSource);
        enumSetValidator.initialize(createAnnotation(TestEnum.class, ignoreCase, allowNull));
        return enumSetValidator;
    }

    private ConstraintValidatorContext createContext() {
        return new ConstraintValidatorContextImpl(Collections.emptyList(), DefaultClockProvider.INSTANCE, PathImpl.createPathFromString("target"), mock(ConstraintDescriptor.class), null);
    }

    @SuppressWarnings("unchecked")
    private IsEnum createAnnotation(Class<TestEnum> myEnum, boolean ignoreCase, boolean allowNull) {
        IsEnum mockAnnotation = mock(IsEnum.class);
        when(mockAnnotation.enumClass()).thenReturn((Class) myEnum);
        when(mockAnnotation.ignoreCase()).thenReturn(ignoreCase);
        when(mockAnnotation.allowNull()).thenReturn(allowNull);
        when(mockAnnotation.message()).thenReturn("{unknown.enum.value}");
        return mockAnnotation;
    }

    private Set<String> createSet(String... values) {
        return new HashSet<String>() {{
            addAll(Arrays.asList(values));
        }};
    }
}