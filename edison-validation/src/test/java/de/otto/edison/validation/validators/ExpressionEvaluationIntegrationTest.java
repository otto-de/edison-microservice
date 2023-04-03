package de.otto.edison.validation.validators;

import de.otto.edison.validation.configuration.ValidationConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = ValidationConfiguration.class)
public class ExpressionEvaluationIntegrationTest {

    @Autowired
    private Validator validator;

    @Test
    void shouldNotEvaluateELInConstraintViolationErrorMessage() {
        TestClass testClass = new TestClass();
        testClass.validateMe = "${1 == 1 ? 'abc' : 'def'}";

        //when
        Set<ConstraintViolation<TestClass>> constraintViolations = validator.validate(testClass);

        //then
        assertThat(constraintViolations, hasSize(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("error message where input is reflected in output: ${1 == 1 ? 'abc' : 'def'}"));
    }


    static class TestClass {

        @TestELInjectionValidation
        String validateMe;
    }
}
