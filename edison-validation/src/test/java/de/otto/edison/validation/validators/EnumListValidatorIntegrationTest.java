package de.otto.edison.validation.validators;

import de.otto.edison.validation.configuration.ValidationConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = ValidationConfiguration.class)
public class EnumListValidatorIntegrationTest {

    @Autowired
    private Validator validator;

    @Test
    public void shouldReturnCorrectConstraintViolationMessage() {
        //given
        TestClass testClass = new TestClass(Arrays.asList("foo", "bar", "nofoo"));

        //when
        Set<ConstraintViolation<TestClass>> constraintViolations = validator.validate(testClass);

        //then
        assertThat(constraintViolations, hasSize(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Unbekannte Enum-Werte: nofoo."));
    }

    @Test
    public void shouldReturnCorrectConstraintViolationMessageWithMultipleValues() {
        //given
        TestClass testClass = new TestClass(Arrays.asList("foo", "nobar", "nofoo"));

        //when
        Set<ConstraintViolation<TestClass>> constraintViolations = validator.validate(testClass);

        //then
        assertThat(constraintViolations, hasSize(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Unbekannte Enum-Werte: nobar,nofoo."));
    }

    enum TestEnum {
        foo,
        bar
    }

    private class TestClass {
        @IsEnum(enumClass = TestEnum.class)
        private List<String> someEnums;

        public TestClass(List<String> someEnums) {
            this.someEnums = someEnums;
        }
    }

}
