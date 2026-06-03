package de.otto.edison.validation.validators;

import de.otto.edison.validation.configuration.ValidationConfiguration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = {ValidationConfiguration.class, ExpressionEvaluationIntegrationTest.TestConfig.class})
public class ExpressionEvaluationIntegrationTest {

    @Configuration
    static class TestConfig {
        @Bean
        public tools.jackson.databind.ObjectMapper objectMapper() {
            return new tools.jackson.databind.ObjectMapper();
        }
    }

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
