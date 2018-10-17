package de.otto.edison.validation.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Objects;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SafeIdValidatorTest {

    private final SafeIdValidator validator = new SafeIdValidator();

    @ParameterizedTest
    @MethodSource("data")
    public void testAllExampleIdValidPairs(final String id, final boolean expected) {
        assertThat(validator.isValid(Objects.toString(id), null), is(expected));
    }

    private static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("id", true),
                Arguments.of("long-id", true),
                Arguments.of("long-id-with-numbers-1234567890", true),
                Arguments.of("long-id-with-numbers-1234567890-and-underscore_", true),
                Arguments.of("ID-with-CAPITALS", true),
                Arguments.of(null, true),
                Arguments.of("id<", false),
                Arguments.of("id>", false),
                Arguments.of("id!", false),
                Arguments.of("id@", false),
                Arguments.of("id#", false),
                Arguments.of("id$", false),
                Arguments.of("id%", false),
                Arguments.of("id^", false),
                Arguments.of("id&", false),
                Arguments.of("id*", false),
                Arguments.of("id(", false),
                Arguments.of("id)", false),
                Arguments.of("id+", false),
                Arguments.of("id=", false)
        );
    }
}
