package de.otto.edison.validation.validators;

import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EnumValidatorTest {

    enum TestEnum {
        foo,
        bar
    }

    @Test
    public void shouldValidateSuccessfully() throws Exception {
        EnumValidator enumValidator =  createAndInitializeValidator(false, false);
        boolean valid = enumValidator.isValid("foo", null);
        assertThat(valid, is(true));
    }

    @Test
    public void shouldFailForInvalidValue() throws Exception {
        EnumValidator enumValidator =  createAndInitializeValidator(false, false);
        boolean valid = enumValidator.isValid("xxx", null);
        assertThat(valid, is(false));
    }

    @Test
    public void shouldFailWhenNotIgnoringCase() throws Exception {
        EnumValidator enumValidator =  createAndInitializeValidator(false, false);
        boolean valid = enumValidator.isValid("Foo", null);
        assertThat(valid, is(false));
    }

    @Test
    public void shouldSucceedWhenIgnoringCase() throws Exception {
        EnumValidator enumValidator =  createAndInitializeValidator(true, false);
        boolean valid = enumValidator.isValid("Foo", null);
        assertThat(valid, is(true));
    }

    @Test
    public void shouldFailForNull() {
        EnumValidator enumValidator =  createAndInitializeValidator(true, false);
        boolean valid = enumValidator.isValid(null, null);
        assertThat(valid, is(false));
    }

    @Test
    public void shouldAllowNullWhenFlagIsSet() {
        EnumValidator enumValidator =  createAndInitializeValidator(true, true);
        boolean valid = enumValidator.isValid(null, null);
        assertThat(valid, is(true));
    }

    @Test
    public void shouldFailForEmptyString() {
        EnumValidator enumValidator =  createAndInitializeValidator(true, false);
        boolean valid = enumValidator.isValid("", null);
        assertThat(valid, is(false));
    }

    private EnumValidator createAndInitializeValidator(boolean ignoreCase, boolean allowNull) {
        EnumValidator enumValidator = new EnumValidator();
        enumValidator.initialize(createAnnotation(TestEnum.class, ignoreCase, allowNull));
        return enumValidator;
    }

    @SuppressWarnings("unchecked")
    private IsEnum createAnnotation(Class<TestEnum> myEnum, boolean ignoreCase, boolean allowNull) {
        IsEnum mockAnnotation = mock(IsEnum.class);
        when(mockAnnotation.enumClass()).thenReturn((Class) myEnum);
        when(mockAnnotation.ignoreCase()).thenReturn(ignoreCase);
        when(mockAnnotation.allowNull()).thenReturn(allowNull);

        return mockAnnotation;
    }
}