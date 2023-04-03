package de.otto.edison.validation.validators;

import org.hibernate.validator.internal.engine.DefaultClockProvider;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnumListValidatorTest {

    enum TestEnum {
        foo,
        bar
    }

    @Test
    public void shouldValidateSuccessfully() throws Exception {
        EnumListValidator enumListValidator = createAndInitializeValidator(false, false);
        boolean valid = enumListValidator.isValid(Arrays.asList("foo", "bar"), createContext());
        assertThat(valid, is(true));
    }

    @Test
    public void shouldFailForInvalidValue() throws Exception {
        EnumListValidator enumListValidator = createAndInitializeValidator(false, false);
        boolean valid = enumListValidator.isValid(Arrays.asList("foo", "xxx"), createContext());
        assertThat(valid, is(false));
    }

    @Test
    public void shouldFailWhenNotIgnoringCase() throws Exception {
        EnumListValidator enumListValidator = createAndInitializeValidator(false, false);
        ConstraintValidatorContext context = createContext();
        boolean valid = enumListValidator.isValid(Collections.singletonList("Foo"), context);
        assertThat(valid, is(false));
    }

    @Test
    public void shouldSucceedWhenIgnoringCase() throws Exception {
        EnumListValidator enumListValidator = createAndInitializeValidator(true, false);
        boolean valid = enumListValidator.isValid(Collections.singletonList("Foo"), createContext());
        assertThat(valid, is(true));
    }

    @Test
    public void shouldFailForNull() {
        EnumListValidator enumListValidator = createAndInitializeValidator(true, false);
        boolean valid = enumListValidator.isValid(null, createContext());
        assertThat(valid, is(false));
    }

    @Test
    public void shouldFailForNullValue() {
        EnumListValidator enumListValidator = createAndInitializeValidator(true, false);
        boolean valid = enumListValidator.isValid(Collections.singletonList((String) null), createContext());
        assertThat(valid, is(false));
    }

    @Test
    public void shouldAllowNullWhenFlagIsSet() {
        EnumListValidator enumListValidator = createAndInitializeValidator(true, true);
        boolean valid = enumListValidator.isValid(null, createContext());
        assertThat(valid, is(true));
    }

    @Test
    public void shouldFailForEmptyString() {
        EnumListValidator enumListValidator = createAndInitializeValidator(true, false);
        boolean valid = enumListValidator.isValid(Collections.singletonList(""), createContext());
        assertThat(valid, is(false));
    }

    private EnumListValidator createAndInitializeValidator(boolean ignoreCase, boolean allowNull) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("ValidationMessages");
        messageSource.setUseCodeAsDefaultMessage(true);

        EnumListValidator enumListValidator = new EnumListValidator(messageSource);
        enumListValidator.initialize(createAnnotation(TestEnum.class, ignoreCase, allowNull));
        return enumListValidator;
    }

    private ConstraintValidatorContext createContext() {
        return new ConstraintValidatorContextImpl(DefaultClockProvider.INSTANCE, PathImpl.createPathFromString("target"), mock(ConstraintDescriptor.class), null, ExpressionLanguageFeatureLevel.DEFAULT, ExpressionLanguageFeatureLevel.DEFAULT);
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

}